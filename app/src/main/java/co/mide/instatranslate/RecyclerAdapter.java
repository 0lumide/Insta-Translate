package co.mide.instatranslate;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import co.mide.instatranslate.data.DataStore;
import co.mide.instatranslate.data.LanguagePair;
import co.mide.translator.Language;

/**
 * Adapter for the recycler view
 * Created by Olumide on 1/8/2016.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private static ArrayList<LanguagePair> adapterData;
    Context context;

    public RecyclerAdapter(Context context) {
        this.context = context.getApplicationContext();
        adapterData = DataStore.getLanguagePairs(context);
    }

    public void add(Language source, Language dest){
        adapterData.add(new LanguagePair(source, dest));
        saveData();
        this.notifyItemInserted(adapterData.size() - 1);
    }

    public void itemDismissed(final int position){
        remove(position);
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
                itemDismissed(holder.getAdapterPosition());
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

    public void saveData(){
        DataStore.updateLanguagePairs(context, adapterData);
    }
}