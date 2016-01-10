package co.mide.translator;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 *The Google translate api web request interface for retrofit
 */
public interface GoogleTranslate {
    @GET("v2")
    // ?key=INSERT-YOUR-KEY&target=de&q=Hello%20world
    Call<TranslateResult> translate(
            @Query("key") String apiKey,
            @Query("target") String targetLanguage,
            @Query("q") String phrase
    );

    @GET("v2/detect")
    Call<LangDetectionResult> languageDetect(
            @Query("key") String apiKey,
            @Query("q") String phrase
    );

    @GET("v2/languages")
    Call<LanguagesResult> getSupportedLanguages(
            @Query("key") String apiKey,
            @Query("target") String target
    );
}
