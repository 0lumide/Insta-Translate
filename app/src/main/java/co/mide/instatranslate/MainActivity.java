package co.mide.instatranslate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.solovyev.android.views.llm.LinearLayoutManager;

import java.util.ArrayList;

import co.mide.clipbroadcast.ClipMonitor;
import co.mide.instatranslate.views.EmptyRecyclerView;
import co.mide.translator.Language;
import co.mide.translator.Translator;

public class MainActivity extends AppCompatActivity {
    ProgressDialog dialog;
    EmptyRecyclerView recyclerView;
    RecyclerAdapter recyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new ClipMonitor(this).start();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewAdapter = new RecyclerAdapter(this);
        recyclerView = (EmptyRecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setEmptyView(findViewById(R.id.empty_view));

        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        final LinearLayoutManager layoutManager = new org.solovyev.android.views.llm.LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(recyclerViewAdapter);
        dialog = ProgressDialog.show(this, "",
                getString(R.string.please_wait), true);
        Translator t = new Translator(getString(R.string.google_translate_api_key));
        t.getLanguages("en", new Translator.onGetLanguagesComplete() {
            @Override
            public void getLanguageComplete(ArrayList<Language> languages) {
                for (int i = 0; i < languages.size(); i++)
                    System.out.printf("%s: %s\n", languages.get(i).language, languages.get(i).name);
                dialog.dismiss();
            }

            @Override
            public void error() {
                System.out.println("Error");
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
        recyclerViewAdapter.saveData();
    }

    public void launch(View v){
        Intent intent = new Intent(this, DefinitionActivity.class);
        intent.putExtra(ClipMonitor.COPIED_STRING, "Hola, ¿entiendes Inglés?");
        startActivity(intent);
    }
}
