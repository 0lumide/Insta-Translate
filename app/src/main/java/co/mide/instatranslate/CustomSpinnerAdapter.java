package co.mide.instatranslate;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import co.mide.translator.Language;

/**
 * Adapter for the Language spinners
 * Created by Olumide on 1/11/2016.
 */
public class CustomSpinnerAdapter extends BaseAdapter{
    ArrayList<Language> languages;
    Context context;

    public CustomSpinnerAdapter(Context c, ArrayList<Language> languages){
        this.context = c;
        this.languages = languages;
    }

    @Override
    public int getCount(){
        return languages.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if((convertView != null) && convertView instanceof TextView){
            ((TextView)convertView).setText(languages.get(position).getName());
            return convertView;
        }else{
            TextView textView = (TextView)View.inflate(context, R.layout.spinner_textview, null);
            textView.setText(languages.get(position).getName());
            return textView;
        }
    }

    @Override
    public long getItemId(int position){
        return -1;
    }

    @Override
    public Object getItem(int position){
        return languages.get(position);
    }
}
