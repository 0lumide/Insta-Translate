package co.mide.instatranslate;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import co.mide.translator.Language;

/**
 * Adapter for the recycler view
 * Created by Olumide on 1/8/2016.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private static ArrayList<LanguagePair> adapterData;
    static final String LANG_PAIRS = "languagePairs";
    static final String LANG_PAIRS_JSON = "language_pairs_json";
    private static SharedPreferences sharedPreferences;
    Context context;

    public RecyclerAdapter(Context context) {
        this.context = context.getApplicationContext();
        sharedPreferences = this.context.getSharedPreferences(LANG_PAIRS, Context.MODE_PRIVATE);
        adapterData = getLanguagePairs(context);
    }

    public static ArrayList<LanguagePair> getLanguagePairs(Context context){
        if(adapterData != null)
            return adapterData;
        if(sharedPreferences == null)
            sharedPreferences = context.getApplicationContext().getSharedPreferences(LANG_PAIRS, Context.MODE_PRIVATE);
        adapterData = (new Gson()).fromJson(sharedPreferences.getString(LANG_PAIRS_JSON, null), new TypeToken<ArrayList<LanguagePair>>() {}.getType());
        if(adapterData == null)
            adapterData = new ArrayList<>();
        return adapterData;
    }

    public void add(Language source, Language dest){
        adapterData.add(new LanguagePair(source, dest));
        saveData();
        this.notifyItemInserted(adapterData.size() - 1);
    }

    public void itemDismissed(final int position, View rowView){
        Animation anim = AnimationUtils.loadAnimation(context,
                android.R.anim.slide_out_right);
        anim.setDuration(200);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                remove(position);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        rowView.startAnimation(anim);
    }

    public void remove(int index){
        adapterData.remove(index);
        saveData();
        notifyItemRemoved(index);
    }

    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        CardView v = (CardView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.language_view, parent, false);
        // set the view's size, margins, padding and layout parameters
        return new RecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.destLang.setText(adapterData.get(position).getDestLanguage().name);
        holder.dismissImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemDismissed(holder.getAdapterPosition(), holder.itemView);
            }
        });
        holder.sourceLang.setText(adapterData.get(position).getSourceLanguage().name);
    }

    @Override
    public int getItemCount() {
        return adapterData.size();
    }

    public LanguagePair getItem(int index) {
        return adapterData.get(index);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView sourceLang;
        public TextView destLang;
        public boolean isEmptyView;
        public ImageButton dismissImageButton;

        public ViewHolder(View view){
            super(view);
            dismissImageButton = (ImageButton)view.findViewById(R.id.dismiss_image_button);
            sourceLang = (TextView)view.findViewById(R.id.source_language_text_view);
            destLang = (TextView)view.findViewById(R.id.dest_language_text_view);
            isEmptyView = (destLang == null);
        }
    }

    public static class LanguagePair{
        private Language sourceLanguage, destLanguage;

        public LanguagePair(Language source, Language dest){
            sourceLanguage = source;
            destLanguage = dest;
        }

        public Language getSourceLanguage(){
            return sourceLanguage;
        }

        public  Language getDestLanguage(){
            return destLanguage;
        }
    }

    public void saveData(){
        sharedPreferences.edit().putString(LANG_PAIRS_JSON, (new Gson()).toJson(adapterData)).apply();
    }
}
