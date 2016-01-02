package co.mide.translator;

import android.support.annotation.NonNull;

import com.neovisionaries.i18n.*;

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
    final String GOOGLE_TRANSLATE_ENDPOINT = "https://www.googleapis.com/language/translate/v2/";
    private GoogleTranslate translator;

    public Translator(String apiKey){
        this.key = apiKey;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(GOOGLE_TRANSLATE_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        translator = retrofit.create(GoogleTranslate.class);
    }

    public void translate(@NonNull String originalText, @NonNull String iso69, @NonNull final onTranslateComplete callback){
        Call<TranslateResult> call = translator.translate(key, iso69, originalText);
        call.enqueue(new Callback<TranslateResult>() {
            @Override
            public void onResponse(Response<TranslateResult> response, Retrofit retrofit) {
                if(response.body() == null){
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

    /**
     * Function that returns the name of the Language based off of the iso-69 code
     * @param iso69 the string that is used to represent languages
     * @return the name of the language
     */
    public String getLanguageName(String iso69){
        LanguageAlpha3Code language  = LanguageAlpha3Code.getByCode(iso69);
        return language.getName();
    }

    /**
     * Interface that contains the translate complete callback
     */
    public interface onTranslateComplete{
        void translateComplete(String translated);
        void error();
    }
}
