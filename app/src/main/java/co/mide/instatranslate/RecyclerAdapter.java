package co.mide.instatranslate;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Adapter for the recycler view
 * Created by Olumide on 1/8/2016.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<LanguagePair> adapterData;
    static final String DEFAULT_JSON = "{\"languagePairs\": []}";
    static final String SOURCE_LANG = "sourceLanguage";
    static final String DEST_LANG = "destLanguage";
    static final String LANG_PAIRS = "languagePairs";
    static final String LANG_PAIRS_JSON = "language_pairs_json";
    private SharedPreferences sharedPreferences;

    public RecyclerAdapter(Context context) {
        adapterData = new ArrayList<>();
        sharedPreferences = context.getSharedPreferences(LANG_PAIRS, Context.MODE_PRIVATE);
        try {
            //Should prop just use google GSON
            JSONObject jsonObject = new JSONObject(sharedPreferences.getString(LANG_PAIRS_JSON, DEFAULT_JSON));
            JSONArray jsonArray = jsonObject.getJSONArray(LANG_PAIRS);
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject jObject = jsonArray.getJSONObject(i);
                String sourceLang = jObject.getString(SOURCE_LANG);
                String destLang = jObject.getString(DEST_LANG);
                adapterData.add(new LanguagePair(sourceLang, destLang));
                Log.e("add", "add: "+i);
            }
            adapterData.add(new LanguagePair("Spanish", "English"));
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void remove(int index){
        adapterData.remove(index);
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
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.destLang.setText(adapterData.get(position).getDestLanguage());
        holder.sourceLang.setText(adapterData.get(position).getSourceLanguage());
    }

    @Override
    public int getItemCount() {
        return adapterData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView sourceLang;
        public TextView destLang;
        public boolean isEmptyView;

        public ViewHolder(View view){
            super(view);
            sourceLang = (TextView)view.findViewById(R.id.source_language_text_view);
            destLang = (TextView)view.findViewById(R.id.dest_language_text_view);
            isEmptyView = (destLang == null);
        }
    }

    public static class LanguagePair{
        private String sourceLanguage, destLanguage;

        public LanguagePair(String source, String dest){
            sourceLanguage = source;
            destLanguage = dest;
        }

        public String getSourceLanguage(){
            return sourceLanguage;
        }

        public  String getDestLanguage(){
            return destLanguage;
        }
    }

    public void saveData(){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for(int i = 0; i < adapterData.size(); i++){
                LanguagePair languagePair = adapterData.get(i);
                JSONObject jObject = new JSONObject();
                jObject.put(SOURCE_LANG, languagePair.getSourceLanguage());
                jObject.put(DEST_LANG, languagePair.getDestLanguage());
                jsonArray.put(jObject);
                Log.e("size", "size: "+i);
            }
            jsonObject.put(LANG_PAIRS, jsonArray);
            Log.e("save","ssave");
            sharedPreferences.edit().putString(LANG_PAIRS_JSON, jsonObject.toString()).apply();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}
