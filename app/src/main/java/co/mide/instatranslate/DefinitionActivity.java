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

public class DefinitionActivity extends Activity {
    private TextView sourceTextView;
    private TextView translatedTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);
        View.OnClickListener close = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        };
        View cardView = findViewById(R.id.card_view);
        sourceTextView = (TextView)findViewById(R.id.source_content);
        translatedTextView = (TextView)findViewById(R.id.dest_content);
        cardView.getParent().requestDisallowInterceptTouchEvent(true);
        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        findViewById(R.id.close_button).setOnClickListener(close);
        findViewById(R.id.background).setOnClickListener(close);
        sourceTextView.setText(getIntent().getStringExtra(ClipMonitor.COPIED_STRING));
        findViewById(R.id.copy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyTranslationToClipBoard();
                Toast.makeText(DefinitionActivity.this, getString(R.string.translation_copied), Toast.LENGTH_LONG).show();
            }
        });
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
}
