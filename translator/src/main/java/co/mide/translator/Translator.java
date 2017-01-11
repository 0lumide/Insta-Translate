package co.mide.translator;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.Html;

import java.util.ArrayList;
import java.util.Locale;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Public facing class that further abstracts the inner workings of the library
 * i.e a simple class to be used for translating stuff
 * Created by Olumide on 12/30/2015.
 */
@SuppressWarnings("unused")
public class Translator {
    private String key;
    final String GOOGLE_TRANSLATE_ENDPOINT = "https://www.googleapis.com/language/translate/";
    private GoogleTranslate translator;

    public Translator(String apiKey){
        this.key = apiKey;
        Retrofit retrofit = new Retrofit.Builder()
                .client(new OkHttpClient())
                .baseUrl(GOOGLE_TRANSLATE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        translator = retrofit.create(GoogleTranslate.class);
    }

    public void translate(@NonNull String originalText, @NonNull String iso639, @NonNull final onTranslateComplete callback){
        Call<TranslateResult> call = translator.translate(key, iso639, originalText);
        call.enqueue(new Callback<TranslateResult>() {
            @Override
            public void onResponse(Call<TranslateResult> call, Response<TranslateResult> response) {
                if(response.body() == null){
                    System.err.printf("message: %s\n", response.message());
                    System.err.printf("url: %s\n", response.raw().toString());
                    callback.error(response.message());
                    return;
                }
                String translated = response.body().data.translations.get(0).translatedText;
                String detectedSourceLang = response.body().data.translations.get(0).detectedSourceLanguage;
                callback.translateComplete(htmlDecode(translated), detectedSourceLang);
            }

            @Override
            public void onFailure(Call<TranslateResult> call, Throwable t) {
                t.printStackTrace();
                callback.error(t.getMessage());
            }
        });
    }

    public void detectLanguage(@NonNull String originalText, @NonNull final onLanguageDetected callback){
        Call<LangDetectionResult> call = translator.languageDetect(key, originalText);
        call.enqueue(new Callback<LangDetectionResult>() {
            @Override
            public void onResponse(Call<LangDetectionResult> call, Response<LangDetectionResult> response) {
                if(response.body() == null){
                    System.out.printf("message: %s\n", response.message());
                    System.out.printf("url: %s\n", response.raw().toString());
                    callback.error(response.message());
                    return;
                }
                callback.languageDetected(response.body().data.detections.get(0).get(0).language);
            }

            @Override
            public void onFailure(Call<LangDetectionResult> call, Throwable t) {
                t.printStackTrace();
                callback.error(t.getMessage());
            }
        });
    }

    private String htmlDecode(String string) {
        if (Build.VERSION.SDK_INT >= 24) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(string).toString();
        }
    }

    public void getLanguages(@NonNull String iso639, @NonNull final onGetLanguagesComplete callback){
        Call<LanguagesResult> call = translator.getSupportedLanguages(key, iso639);
        call.enqueue(new Callback<LanguagesResult>() {
            @Override
            public void onResponse(Call<LanguagesResult> call, Response<LanguagesResult> response) {
                if(response.body() == null){
                    System.err.printf("message: %s\n", response.message());
                    System.err.printf("url: %s\n", response.raw().toString());
                    callback.error(response.message());
                    return;
                }
                callback.getLanguageComplete(response.body().data.languages);
            }

            @Override
            public void onFailure(Call<LanguagesResult> call, Throwable t) {
                t.printStackTrace();
                callback.error(t.getMessage());
            }
        });
    }

    /**
     * Function that returns the name of the Language based off of the iso-639 code
     * @param iso639 the string that is used to represent languages
     * @return the name of the language
     */
    public static String getLanguageName(String iso639){
        Locale loc = new Locale(iso639);
        return loc.getDisplayLanguage(loc);
    }

    /**
     * Interface that contains the translate complete callback
     */
    public interface onTranslateComplete{
        void translateComplete(String translated, String detectedIso639);
        void error(String message);
    }

    /**
     * Interface that contains the getLanguages callback
     */
    public interface onGetLanguagesComplete{
        void getLanguageComplete(ArrayList<Language> languages);
        void error(String message);
    }

    /**
     * Interface that contains the language detected complete callback
     */
    public interface onLanguageDetected{
        /**
         *
         * @param detectedIso639 This is the language that was detected.
         *                      It is represented by its iso639-1 code.
         *                      It could also be null if no language could be detected
         */
        void languageDetected(String detectedIso639);
        void error(String message);
    }
}
