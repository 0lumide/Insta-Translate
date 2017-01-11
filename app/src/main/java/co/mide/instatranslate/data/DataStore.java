package co.mide.instatranslate.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.mide.instatranslate.LanguagePair;
import co.mide.translator.Language;


public class DataStore {
    private static final String KEY_LANG_PAIRS = "languagePairs";
    private static final String KEY_LANG_PAIRS_JSON = "language_pairs_json";
    private static final String KEY_DEF_DEST_LANG = "co.mide.instatranslate.DEF_DEST_LANG";
    private static final String KEY_TRANSLATE_COUNT = "co.mide.instatranslate.TRANSLATE_COUNT";

    private DataStore() {

    }

    public static ArrayList<LanguagePair> getLanguagePairs(Context context){
        SharedPreferences sharedPreferences = getSharedPrefs(context);
        Gson gson = new Gson();
        Type typeToken = new TypeToken<ArrayList<LanguagePair>>() {}.getType();
        ArrayList<LanguagePair> languagePairs =
                gson.fromJson(sharedPreferences.getString(KEY_LANG_PAIRS_JSON, null), typeToken);
        if(languagePairs == null) {
            languagePairs = new ArrayList<>();
        }
        return languagePairs;
    }

    public static void updateLanguagePairs(Context context, List<LanguagePair> languagePairs){
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPrefs(context);
        sharedPreferences.edit().putString(KEY_LANG_PAIRS_JSON, gson.toJson(languagePairs)).apply();
    }

    public static Language getDefaultDestLang(Context context) {
        SharedPreferences prefs = getSharedPrefs(context);
        Gson gson = new Gson();
        Language language = gson.fromJson(prefs.getString(KEY_DEF_DEST_LANG, null), Language.class);
        if(language == null) {
            // use system language if not set
            String langName = Locale.getDefault().getDisplayLanguage();
            String iso639_2 = Locale.getDefault().getLanguage();
            language = new Language(iso639_2, langName);
        }
        return language;
    }

    public static void updateDefaultDestLang(Context context, Language language) {
        SharedPreferences prefs = getSharedPrefs(context);
        Gson gson = new Gson();
        prefs.edit().putString(KEY_DEF_DEST_LANG, gson.toJson(language)).apply();
    }

    public static int getTranslationCount(Context context) {
        SharedPreferences prefs = getSharedPrefs(context);
        return prefs.getInt(KEY_TRANSLATE_COUNT, 0);
    }

    public synchronized static void updateTranslationCount(Context context) {
        int oldCount = getTranslationCount(context);
        SharedPreferences prefs = getSharedPrefs(context);
        prefs.edit().putInt(KEY_TRANSLATE_COUNT, oldCount+1).apply();
    }

    private static SharedPreferences getSharedPrefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(KEY_LANG_PAIRS, Context.MODE_PRIVATE);
    }
}
