package co.mide.instatranslate;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import co.mide.clipbroadcast.ClipMonitor;

public class DefinitionActivity extends Activity {
    private TextView translatedTextView;
    private static final String SOURCE_TEXT = "SOURCE_TEXT";
    private static final String TRANSLATED_TEXT = "TRANSLATED_TEXT";
    private static final String DEST_LANG_NAME = "DEST_LANG_NAME";
    private static final String SOURCE_LANG_NAME = "SOURCE_LANG_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);

        TextView sourceTextView = (TextView)findViewById(R.id.source_content);
        TextView sourceLangTextView = (TextView)findViewById(R.id.source_language);
        TextView destLangTextView = (TextView)findViewById(R.id.dest_langage);
        translatedTextView = (TextView)findViewById(R.id.dest_content);
        View cardView = findViewById(R.id.card_view);

        cardView.getParent().requestDisallowInterceptTouchEvent(true);
        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        // Set the texts
        sourceLangTextView.setText(getIntent().getStringExtra(SOURCE_LANG_NAME));
        destLangTextView.setText(getIntent().getStringExtra(DEST_LANG_NAME));

        sourceTextView.setText(getIntent().getStringExtra(SOURCE_TEXT));
        translatedTextView.setText(getIntent().getStringExtra(TRANSLATED_TEXT));

        // set listeners
        findViewById(R.id.copy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTranslationToClipBoard();
                Toast.makeText(DefinitionActivity.this, getString(R.string.translation_copied), Toast.LENGTH_LONG).show();
            }
        });

        View.OnClickListener close = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        findViewById(R.id.close_button).setOnClickListener(close);
        findViewById(R.id.background).setOnClickListener(close);
    }

    private void copyTranslationToClipBoard(){
        CharSequence translated = translatedTextView.getText();
        String[] mimeTypes = new String[]{ClipMonitor.MIME_IGNORE, ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipDescription clipDescription = new ClipDescription("Text from Clipboard", mimeTypes);
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

    public static Intent builtIntent(Context context, String sourceText, String sourceLangName,
                                     String transText, String destLangName) {
        Intent intent = new Intent(context, DefinitionActivity.class);
        intent.putExtra(SOURCE_TEXT, sourceText);
        intent.putExtra(TRANSLATED_TEXT, transText);
        intent.putExtra(DEST_LANG_NAME, destLangName);
        intent.putExtra(SOURCE_LANG_NAME, sourceLangName);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
