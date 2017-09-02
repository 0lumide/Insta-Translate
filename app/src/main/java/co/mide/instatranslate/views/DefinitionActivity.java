package co.mide.instatranslate.views;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.content.ClipboardManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import co.mide.clipbroadcast.ClipMonitor;
import co.mide.instatranslate.R;
import co.mide.instatranslate.data.DataStore;
import co.mide.instatranslate.data.LanguagePair;
import co.mide.translator.Language;
import co.mide.translator.Translator;

import static android.speech.tts.TextToSpeech.LANG_AVAILABLE;
import static android.speech.tts.TextToSpeech.LANG_COUNTRY_AVAILABLE;
import static android.speech.tts.TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;

public class DefinitionActivity extends Activity {
    private TextView translatedTextView;
    private TextView sourceLangTextView;
    private TextView sourceTextView;
    private TextView destLangTextView;
    private ContentLoadingProgressBar loadingProgressBar;
    private TextToSpeech textToSpeech;

    private static final String SOURCE_TEXT = "SOURCE_TEXT";
    private static final String TRANSLATED_TEXT = "TRANSLATED_TEXT";
    private static final String DEST_LANG_NAME = "DEST_LANG_NAME";
    private static final String SOURCE_LANG_NAME = "SOURCE_LANG_NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_definition);

        sourceTextView = (TextView) findViewById(R.id.source_content);
        sourceLangTextView = (TextView) findViewById(R.id.source_language);
        destLangTextView = (TextView) findViewById(R.id.dest_langage);
        translatedTextView = (TextView) findViewById(R.id.dest_content);
        loadingProgressBar = (ContentLoadingProgressBar)findViewById(R.id.loading);
        View cardView = findViewById(R.id.card_view);

        cardView.getParent().requestDisallowInterceptTouchEvent(true);
        cardView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });


        Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            if (getString(R.string.mime_plain_text).equals(intent.getType())
                    || getString(R.string.mime_html).equals(intent.getType())) {
                handleExternalIntent(intent); // Handle text being sent
            }
        } else {
            String sourceIso = getIntent().getStringExtra(SOURCE_LANG_NAME);
            String destIso = getIntent().getStringExtra(DEST_LANG_NAME);

            String sourceLangName = Translator.getLanguageName(sourceIso).toUpperCase();
            Language sourceLang = new Language(sourceIso, sourceLangName);
            String destLangName = Translator.getLanguageName(destIso).toUpperCase();
            Language destLang = new Language(destIso, destLangName);
            LanguagePair langPair = new LanguagePair(sourceLang, destLang);

            // Set the texts
            String sourceText = getIntent().getStringExtra(SOURCE_TEXT);
            String translatedText = getIntent().getStringExtra(TRANSLATED_TEXT);
            sourceTextView.setText(sourceText);
            sourceLangTextView.setText(sourceLangName);
            destLangTextView.setText(destLangName);

            translatedTextView.setText(getIntent().getStringExtra(TRANSLATED_TEXT));
            initTextToSpeech(langPair, sourceText, translatedText);
        }

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

    private void handleExternalIntent(Intent intent) {
        final String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(text == null || text.isEmpty()) {
            Toast.makeText(this, R.string.invalid_string, Toast.LENGTH_LONG);
            finish();
        } else {
            sourceTextView.setText(text);
            initLoading();
            final Translator t = new Translator(getString(R.string.google_translate_api_key));

            t.detectLanguage(text, new Translator.onLanguageDetected(){
                @Override
                public void languageDetected(String detectedIso639) {
                    Language destLang = null;
                    for(LanguagePair pair: DataStore.getLanguagePairs(DefinitionActivity.this)) {
                        if (pair.getSourceLanguage().language.equals(detectedIso639)) {
                            destLang = pair.getDestLanguage();
                            break;
                        }
                    }
                    if (destLang == null) {
                        destLang = DataStore.getDefaultDestLang(DefinitionActivity.this);
                    }
                    String langName = Translator.getLanguageName(detectedIso639);
                    Language sourceLang = new Language(detectedIso639, langName);
                    LanguagePair langPair = new LanguagePair(sourceLang, destLang);
                    translateAndShow(langPair, text);
                }

                public void error(String message){
                    Toast.makeText(DefinitionActivity.this, R.string.translate_error, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }

    private void speak(Locale locale, String text) {
        textToSpeech.setLanguage(locale);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            textToSpeech.speak(text, QUEUE_FLUSH, null);
        } else {
            textToSpeech.speak(text, QUEUE_FLUSH, null, "unique-key?");
        }
    }

    private void translateAndShow(final LanguagePair languagePair, final String text) {
        final Translator t = new Translator(getString(R.string.google_translate_api_key));
        t.translate(text, languagePair.getDestLanguage().language, new Translator.onTranslateComplete() {
            @Override
            public void translateComplete(final String translated, String detectedIso639) {
                unInitLoading();
                translatedTextView.setText(translated);
                destLangTextView.setText(languagePair.getDestLanguage().name.toUpperCase());
                sourceLangTextView.setText(languagePair.getSourceLanguage().name.toUpperCase());
                initTextToSpeech(languagePair, text, translated);
            }

            public void error(String message) {
                Toast.makeText(DefinitionActivity.this, R.string.translate_error, Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void animateViewVisible(final View view) {
        if(Build.VERSION.SDK_INT < 12) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setAlpha(0);
            view.setVisibility(View.VISIBLE);
            view.animate()
                    .alpha(1.0f)
                    .setDuration(500);
        }
    }

    private void initTextToSpeech(final LanguagePair languagePair, final String text, final String translated) {
        textToSpeech = new TextToSpeech(DefinitionActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    String sourceIso = languagePair.getSourceLanguage().language;
                    String destIso = languagePair.getDestLanguage().language;
                    final Locale sourceLocale = new Locale(sourceIso);
                    final Locale destLocale = new Locale(destIso);
                    // Check if languages are supported
                    int avail = textToSpeech.isLanguageAvailable(sourceLocale);
                    if(avail == LANG_AVAILABLE
                            || avail == LANG_COUNTRY_AVAILABLE
                            || avail == LANG_COUNTRY_VAR_AVAILABLE){
                        ImageButton speakButton = (ImageButton) findViewById(R.id.source_sound);
                        animateViewVisible(speakButton);
                        speakButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                speak(sourceLocale, text);
                            }
                        });
                    }
                    avail = textToSpeech.isLanguageAvailable(destLocale);
                    if(avail == LANG_AVAILABLE
                            || avail == LANG_COUNTRY_AVAILABLE
                            || avail == LANG_COUNTRY_VAR_AVAILABLE){
                        ImageButton speakButton = (ImageButton) findViewById(R.id.dest_sound);
                        animateViewVisible(speakButton);
                        speakButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                speak(destLocale, translated);
                            }
                        });
                    }
                }
            }
        });
    }

    private void initLoading() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        int color = ContextCompat.getColor(this, R.color.empty_text_block);
        sourceLangTextView.setBackgroundColor(color);
        destLangTextView.setBackgroundColor(color);
        findViewById(R.id.dest_content_scrollview).setVisibility(View.GONE);
    }

    private void unInitLoading() {
        loadingProgressBar.setVisibility(View.GONE);
        sourceLangTextView.setBackgroundResource(0);
        destLangTextView.setBackgroundResource(0);
        findViewById(R.id.dest_content_scrollview).setVisibility(View.VISIBLE);
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
    protected void onDestroy() {
        if(textToSpeech != null) {
            textToSpeech.shutdown();
        }
        super.onDestroy();
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
