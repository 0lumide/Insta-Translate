package co.mide.translator;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 *
 */
public interface GoogleTranslate {
//    @GET("?key={key}&target={target}&q={phrase}")
    @GET("https://dl.dropboxusercontent.com/u/66559338/Google-Translate/translated.json")
    // ?key=INSERT-YOUR-KEY&target=de&q=Hello%20world
    Call<TranslateResult> translate(
            @Query("key") String apiKey,
            @Query("target") String targetLanguage,
            @Query("q") String phrase
    );

    // /detect?key=INSERT-YOUR-KEY&q=Google%20Translate%20Rocks
    @GET("detect?key={key}&q={phrase}")
    String languageDetect(
            @Path("key") String apiKey,
            @Path("phrase") String phrase
    );

    // /languages?key=INSERT-YOUR-KEY
    @GET("languages?key={key}")
    List<String> getSupportedLanguages(
            @Path("key") String apiKey
    );
}
