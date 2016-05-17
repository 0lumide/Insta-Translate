package co.mide.instatranslate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import org.solovyev.android.views.llm.LinearLayoutManager;
import co.mide.clipbroadcast.ClipMonitor;
import co.mide.instatranslate.views.EmptyRecyclerView;
import co.mide.translator.Language;
import co.mide.translator.Translator;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import android.support.design.widget.CoordinatorLayout;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ProgressDialog dialog;
    EmptyRecyclerView recyclerView;
    RecyclerAdapter recyclerViewAdapter;
    FloatingActionButton fabAdd;
    FloatingActionButton fabDone;
    CoordinatorLayout pairSelectView;
    Spinner sourceSpinner;
    Spinner destSpinner;
    static final String LANGUAGES = "languages";
    static final String LAST_SAVED_LANGUAGES = "languages_last_saved";
    private SharedPreferences sharedPreferences;
    ArrayList<Language> languageList;
    View.OnTouchListener ignoreTouchListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ClipMonitor(this).start();
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(LANGUAGES, Context.MODE_PRIVATE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewAdapter = new RecyclerAdapter(this);
        recyclerView = (EmptyRecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setEmptyView(findViewById(R.id.empty_view));

        initSwipe().attachToRecyclerView(recyclerView);
        sourceSpinner = (Spinner)findViewById(R.id.source_language_spinner);
        destSpinner = (Spinner)findViewById(R.id.dest_language_spinner);

        fabDone = (FloatingActionButton)findViewById(R.id.fab_done);
        pairSelectView = (CoordinatorLayout)findViewById(R.id.pair_select);
        fabAdd = (FloatingActionButton)findViewById(R.id.fab_add);
        fabAdd.setRippleColor(ContextCompat.getColor(this, R.color.lang_pair_chooser_background));

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                populateSpinners(languageList);
                animateButton(fabAdd, findViewById(R.id.shadow_holder));
                showFade(findViewById(R.id.fade_view));
            }
        });

        fabDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recyclerViewAdapter.add((Language)sourceSpinner.getSelectedItem(),
                        (Language)destSpinner.getSelectedItem());
                onBackPressed();
            }
        });

        ignoreTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        };
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);

        if(isLanguagesUpToDate())
            languageList = getLanguages();
        else
            updateLanguages();
    }

    @Override
    public void onBackPressed(){
        View view = findViewById(R.id.fade_view);
        if(view.getVisibility() != View.VISIBLE)
            super.onBackPressed();
        else if(!(view.getAnimation().hasStarted() && !view.getAnimation().hasEnded())) {
            view.setOnTouchListener(null);
            view.setOnClickListener(null);
            hideLangPairSelect();
        }
    }

    private ArrayList<Language> getLanguages(){
        String languagesString = sharedPreferences.getString(LANGUAGES, null);
        //I have no idea what this TypeToken stuff is
        return (new Gson()).fromJson(languagesString, new TypeToken<ArrayList<Language>>() {}.getType());
    }

    private boolean isLanguagesUpToDate(){
        String languagesString = sharedPreferences.getString(LANGUAGES, null);
        long lastSaved = sharedPreferences.getLong(LAST_SAVED_LANGUAGES, 0);
        return(((System.currentTimeMillis() - lastSaved) < TimeUnit.DAYS.toMillis(2)) &&
                languagesString != null);
    }

    private void updateLanguages(){
        dialog = ProgressDialog.show(this, "",
                getString(R.string.please_wait), true);
        Translator t = new Translator(getString(R.string.google_translate_api_key));
        t.getLanguages("en", new Translator.onGetLanguagesComplete() {
            @Override
            public void getLanguageComplete(ArrayList<Language> languages) {
                languageList = languages;
                sharedPreferences.edit().putString(LANGUAGES, (new Gson()).toJson(languages)).apply();
                for (int i = 0; i < languages.size(); i++)
                    System.out.printf("%s: %s\n", languages.get(i).language, languages.get(i).name);
                dialog.dismiss();
                sharedPreferences.edit().putLong(LAST_SAVED_LANGUAGES, System.currentTimeMillis()).apply();
            }

            @Override
            public void error(String message) {
                System.err.printf("Error: %s\n", message);
                //TODO show dialog saying error with connection
                fabAdd.setClickable(false);
                fabAdd.setAlpha(0.3f);
                ViewCompat.setElevation(fabAdd, 0f);
                fabAdd.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(MainActivity.this, R.color.disabled_fab)));
                dialog.dismiss();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return false;
    }

    private void populateSpinners(ArrayList<Language> languages){
        destSpinner.setAdapter(new CustomSpinnerAdapter(this, languages));
        ArrayList<Language> sourceLanguages = new ArrayList<>(languages.size());
        ArrayList<RecyclerAdapter.LanguagePair> voidedLanguages = new ArrayList<>(recyclerViewAdapter.getItemCount());
        for(int i = 0; i < recyclerViewAdapter.getItemCount(); i++){
            voidedLanguages.add(recyclerViewAdapter.getItem(i));
        }
        for(int i = 0; i < languages.size(); i++){
            sourceLanguages.add(languages.get(i));
            for(int j = 0; j < voidedLanguages.size(); j++){
                if(languages.get(i).name.equals(voidedLanguages.get(j).getSourceLanguage().name)){
                    voidedLanguages.remove(j);
                    sourceLanguages.remove(sourceLanguages.size()-1);
                    break;
                }
            }
        }
        sourceSpinner.setAdapter(new CustomSpinnerAdapter(this, sourceLanguages));
    }

    private ItemTouchHelper initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                ((RecyclerAdapter)recyclerView.getAdapter()).remove(viewHolder.getAdapterPosition());
            }
        };

        return new ItemTouchHelper(simpleItemTouchCallback);
    }

    private void showFade(View v){
        v.setOnTouchListener(ignoreTouchListener);
        Animation anim  = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        anim.setDuration(700);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        v.setVisibility(View.VISIBLE);
        v.setAnimation(anim);
        v.startAnimation(anim);
    }

    private void hideFade(final View v){
        Animation anim  = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        anim.setDuration(700);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.INVISIBLE);
                v.clearAnimation();
            }
        });
        v.startAnimation(anim);
    }

    public void hideLangPairSelect(){
        fabDone.hide();
        hideFade(findViewById(R.id.fade_view));
        View dest = findViewById(R.id.shadow_holder);
        float newX = 0.5f * dest.getWidth();
        float newY = dest.getY() + (0.5f * (getResources().getDimension(R.dimen.select_pair_view_height)));

        animateUnReveal((int) newX, (int) newY, fabAdd);
    }

    private void animateButton(final FloatingActionButton mFloatingButton, View dest) {

        mFloatingButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.lang_pair_chooser_background)));

        final float newX = (0.5f * (dest.getWidth() - mFloatingButton.getHeight()));
        final float newY = (0.5f * (getResources().getDimension(R.dimen.select_pair_view_height) - mFloatingButton.getHeight()));
        ArcTranslateAnimation anim = new ArcTranslateAnimation(0, -newX, 0, -newY);
        anim.setDuration(1000);
        anim.setFillEnabled(true);
        anim.setFillAfter(true);
        mFloatingButton.clearFocus();
        mFloatingButton.clearAnimation();
        mFloatingButton.setVisibility(View.VISIBLE);
        mFloatingButton.invalidate();

        float x = mFloatingButton.getX() + 0.5f*mFloatingButton.getWidth();
        float y = dest.getY() + getResources().getDimension(R.dimen.select_pair_view_height) - 0.5f*mFloatingButton.getHeight();
        animateReveal((int) x, (int) y, mFloatingButton);

//        new Handler().postDelayed(new Runnable() {
//            public void run() {
//                View dest = findViewById(R.id.shadow_holder);
//                float x = 0.5f * dest.getWidth();
//                float y = dest.getY() + (0.5f * (getResources().getDimension(R.dimen.select_pair_view_height)));
//                animateReveal((int) x, (int) y, mFloatingButton);
//            }
//        }, anim.getDuration());
//
//        mFloatingButton.startAnimation(anim);
    }

    private void animateShowButton(final FloatingActionButton mFloatingButton, View dest) {
        mFloatingButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorAccent)));

        float newX = (0.5f * (dest.getWidth() - mFloatingButton.getHeight()));
        float newY = (0.5f * (getResources().getDimension(R.dimen.select_pair_view_height) - mFloatingButton.getHeight()));

        ArcTranslateAnimation anim = new ArcTranslateAnimation(-newX, 0, -newY, 0);
        anim.setDuration(200);
        anim.setFillAfter(true);
        mFloatingButton.startAnimation(anim);
    }

    private void animateUnReveal(int cx, int cy, final FloatingActionButton mFloatingButton) {
        final View myView = findViewById(R.id.pair_select);
        // get the final radius for the clipping circle
        float finalRadius = hypo(myView.getWidth(), myView.getHeight());

        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, finalRadius, 0);
        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                myView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                myView.setVisibility(View.INVISIBLE);
                mFloatingButton.setVisibility(View.VISIBLE);
                animateShowButton(mFloatingButton, pairSelectView);
            }

            @Override
            public void onAnimationCancel() {}

            @Override
            public void onAnimationRepeat() {}
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.start();
    }

    private void animateReveal(int cx, int cy, final FloatingActionButton mFloatingButton) {
        final View myView = findViewById(R.id.pair_select);

        // get the final radius for the clipping circle
        float finalRadius = hypo(myView.getWidth(), myView.getHeight());

        SupportAnimator animator =
                ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);
        animator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                mFloatingButton.setVisibility(View.INVISIBLE);
                myView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                ((FloatingActionButton)findViewById(R.id.fab_done)).show();
                View view = findViewById(R.id.fade_view);
                view.setOnTouchListener(null);
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        return true;
                    }
                });
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                findViewById(R.id.shadow_holder).setOnTouchListener(ignoreTouchListener);
            }

            @Override
            public void onAnimationCancel() {}

            @Override
            public void onAnimationRepeat() {}
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.start();
    }

    static float hypo(int a, int b) {
        return (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }

    @Override
    public void onPause(){
        super.onPause();
        recyclerViewAdapter.saveData();
    }
}