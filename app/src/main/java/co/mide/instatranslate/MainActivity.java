package co.mide.instatranslate;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
                animateButton(fabAdd, pairSelectView);
                showFade(findViewById(R.id.fade_view));
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);

        if(isLanguagesUpToDate())
            populateSpinners(getLanguages());
        else
            updateLanguages();
    }

    @Override
    public void onBackPressed(){
        if(pairSelectView.getVisibility() != View.VISIBLE)
            super.onBackPressed();
        else
            hideLangPairSelect();
    }

    private ArrayList<Language> getLanguages(){
        String languagesString = sharedPreferences.getString(LANGUAGES, null);
        //I have no idea what this TypeToken stuff is
        return (new Gson()).fromJson(languagesString, new TypeToken<ArrayList<String>>() {}.getType());
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
                populateSpinners(languages);
                sharedPreferences.edit().putString(LANGUAGES, (new Gson()).toJson(languages)).apply();
                for (int i = 0; i < languages.size(); i++)
                    System.out.printf("%s: %s\n", languages.get(i).language, languages.get(i).name);
                dialog.dismiss();
            }

            @Override
            public void error() {
                System.out.println("Error");
                fabAdd.setClickable(false);
                fabAdd.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(MainActivity.this, R.color.disabled_fab)));
                dialog.dismiss();
            }
        });
    }

    private void populateSpinners(ArrayList<Language> languages){
        sourceSpinner.setAdapter(new CustomSpinnerAdapter(this, languages));
        destSpinner.setAdapter(new CustomSpinnerAdapter(this, languages));
    }

    private ItemTouchHelper initSwipe(){
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                ((RecyclerAdapter)recyclerView.getAdapter()).remove(viewHolder.getAdapterPosition());
            }
        };

        return new ItemTouchHelper(simpleItemTouchCallback);
    }

    private void showFade(View v){
        v.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        v.setAlpha(0);
        v.setVisibility(View.VISIBLE);
        v.animate().alpha(1.0f).setDuration(700).start();
    }

    private void hideFade(View v){
        v.setAlpha(1);
        v.animate().alpha(0f).setDuration(700).start();
        v.setVisibility(View.INVISIBLE);
    }

    public void hideLangPairSelect(){
        fabDone.hide();
        hideFade(findViewById(R.id.fade_view));
        animateUnReveal((int) (fabAdd.getX() + 0.5 * fabAdd.getWidth()),
                (int) (fabAdd.getY() + 0.5 * fabAdd.getHeight()), fabAdd);
    }

    private void animateButton(final FloatingActionButton mFloatingButton, View dest) {
        float newX = (0.5f * (dest.getWidth() - mFloatingButton.getHeight()));
        float newY = (0.5f * (getResources().getDimension(R.dimen.select_pair_view_height) - mFloatingButton.getHeight()));

        mFloatingButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.lang_pair_chooser_background)));
        mFloatingButton.animate().translationX(-newX).translationY(-newY)
                .setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animateReveal((int) (mFloatingButton.getX() + 0.5 * mFloatingButton.getWidth()),
                        (int) (mFloatingButton.getY() + 0.5 * mFloatingButton.getHeight()), mFloatingButton);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
    }

    private void animateShowButton(final FloatingActionButton mFloatingButton) {
        mFloatingButton.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorAccent)));
        mFloatingButton.animate().translationX(0).translationY(0)
                .setDuration(200).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
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
                animateShowButton(mFloatingButton);
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