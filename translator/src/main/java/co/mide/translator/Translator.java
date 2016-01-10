package co.mide.translator;

import android.support.annotation.NonNull;

import com.neovisionaries.i18n.*;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Public facing class that further abstracts the inner workings of the library
 * i.e a simple class to be used for translating stuff
 * Created by Olumide on 12/30/2015.
 */
public class Translator {
    private String key;
    final String GOOGLE_TRANSLATE_ENDPOINT = "https://www.googleapis.com/language/translate/";
    private GoogleTranslate translator;

    public Translator(String apiKey){
        this.key = apiKey;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_TRANSLATE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        translator = retrofit.create(GoogleTranslate.class);
    }

    public void translate(@NonNull String originalText, @NonNull String iso639, @NonNull final onTranslateComplete callback){
        Call<TranslateResult> call = translator.translate(key, iso639, originalText);
        call.enqueue(new Callback<TranslateResult>() {
            @Override
            public void onResponse(Response<TranslateResult> response, Retrofit retrofit) {
                if(response.body() == null){
                    System.out.printf("message: %s\n", response.message());
                    System.out.printf("url: %s\n", response.raw().toString());
                    callback.error();
                    return;
                }
                callback.translateComplete(response.body().data.translations.get(0).translatedText);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("Failure");
                callback.error();
            }
        });
    }

    public void detectLanguage(@NonNull String originalText, @NonNull final onLanguageDetected callback){
        Call<LangDetectionResult> call = translator.languageDetect(key, originalText);
        call.enqueue(new Callback<LangDetectionResult>() {
            @Override
            public void onResponse(Response<LangDetectionResult> response, Retrofit retrofit) {
                if(response.body() == null){
                    System.out.printf("message: %s\n", response.message());
                    System.out.printf("url: %s\n", response.raw().toString());
                    callback.error();
                    return;
                }
                callback.languageDetected(response.body().data.detections.get(0).get(0).language);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                System.out.println(t.fillInStackTrace().getMessage());
                System.out.println("Failure");
                callback.error();
            }
        });
    }

    public void getLanguages(@NonNull String iso639, @NonNull final onGetLanguagesComplete callback){
        Call<LanguagesResult> call = translator.getSupportedLanguages(key, iso639);
        call.enqueue(new Callback<LanguagesResult>() {
            @Override
            public void onResponse(Response<LanguagesResult> response, Retrofit retrofit) {
                if(response.body() == null){
                    System.out.printf("message: %s\n", response.message());
                    System.out.printf("url: %s\n", response.raw().toString());
                    callback.error();
                    return;
                }
                callback.getLanguageComplete(response.body().data.languages);
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
                System.out.println(t.fillInStackTrace().getMessage());
                System.out.println("Failure");
                callback.error();
            }
        });
    }

    /**
     * Function that returns the name of the Language based off of the iso-639 code
     * @param iso639 the string that is used to represent languages
     * @return the name of the language
     */
    public String getLanguageName(String iso639){
        LanguageAlpha3Code language  = LanguageAlpha3Code.getByCode(iso639);
        return language.getName();
    }

    /**
     * Interface that contains the translate complete callback
     */
    public interface onTranslateComplete{
        void translateComplete(String translated);
        void error();
    }

    /**
     * Interface that contains the getLanguages callback
     */
    public interface onGetLanguagesComplete{
        void getLanguageComplete(ArrayList<Language> languages);
        void error();
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
        void error();
    }
}
