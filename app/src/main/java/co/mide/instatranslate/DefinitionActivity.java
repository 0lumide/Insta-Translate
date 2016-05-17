package co.mide.instatranslate;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.mide.clipbroadcast.ClipMonitor;
import co.mide.translator.Translator;

public class DefinitionActivity extends Activity {
    private TextView translatedTextView;
    private View loading;
    private String sourceIso;
    private String destIso;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);

        TextView sourceTextView = (TextView)findViewById(R.id.source_content);
        TextView sourceLangTextView = (TextView)findViewById(R.id.source_language);
        TextView destLangTextView = (TextView)findViewById(R.id.dest_langage);
        translatedTextView = (TextView)findViewById(R.id.dest_content);
        View cardView = findViewById(R.id.card_view);
        loading = findViewById(R.id.loading);

        View.OnClickListener close = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        findViewById(R.id.close_button).setOnClickListener(close);
        findViewById(R.id.background).setOnClickListener(close);

        loading.setVisibility(View.VISIBLE);
        translatedTextView.setVisibility(View.INVISIBLE);
        cardView.getParent().requestDisallowInterceptTouchEvent(true);
        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        sourceTextView.setText(getIntent().getStringExtra(ClipMonitor.COPIED_STRING));
        sourceLangTextView.setText(getIntent().getStringExtra("SOURCE_LANG_NAME"));
        destLangTextView.setText(getIntent().getStringExtra("DEST_LANG_NAME"));

        destIso = getIntent().getStringExtra("DEST_LANG");
        sourceIso = getIntent().getStringExtra("SOURCE_LANG");

        findViewById(R.id.copy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTranslationToClipBoard();
                Toast.makeText(DefinitionActivity.this, getString(R.string.translation_copied), Toast.LENGTH_LONG).show();
            }
        });
        String sourceText = sourceTextView.getText().toString();
        translate(sourceText, destIso);
    }

    private void copyTranslationToClipBoard(){
        CharSequence translated = translatedTextView.getText();
        ClipDescription clipDescription = new ClipDescription("Text from Clipboard", new String[]{ClipMonitor.MIME_IGNORE, ClipDescription.MIMETYPE_TEXT_PLAIN});
        ClipData.Item clipItem = new ClipData.Item(translated);
        ClipData clipData = new ClipData(clipDescription, clipItem);
        ClipboardManager clipboardManager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(clipData);
    }

    @Override
    public void onStart(){
        super.onStart();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onPause(){
        super.onPause();
        finish();
    }

    @Override
    public void finish(){
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void translate(String sourceText, final String destIso){
        //TODO read destination language

        Translator t = new Translator(getString(R.string.google_translate_api_key));
        t.translate(sourceText, destIso, new Translator.onTranslateComplete() {
            @Override
            public void translateComplete(String translated) {
                translatedTextView.setText(translated);
                loading.setVisibility(View.GONE);
                translatedTextView.setVisibility(View.VISIBLE);
            }

            public void error(String message){
                translatedTextView.setText(getString(R.string.translate_error));
                loading.setVisibility(View.GONE);
                translatedTextView.setTextColor(getResources().getColor(R.color.faint_text_color));
                translatedTextView.setVisibility(View.VISIBLE);
                findViewById(R.id.copy_button).setEnabled(false);
            }
        });
    }
}
