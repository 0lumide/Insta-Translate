package co.mide.translator;

import java.util.Locale;

/**
 * Class used in gson for parsing the json response from google.
 * Created by Olumide on 1/5/2016.
 */
public class Language{
    /**
     * iso639-2 code for the language
     */
    public String language;
    /*
     * the name of the language in the requested language
     */
    public String name;
    private Locale locale;

    public String getLanguage() {
        return getLocale().getLanguage();
    }

    public void setLanguage(String language) {
        setLocale(language);
        this.language = language;
    }

    public String getName() {
        return getLocale().getDisplayName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public Locale getLocale() {
        if (locale == null) {
            setLocale(language);
        }
        return locale;
    }

    public void setLocale(String localeStr) {
        if(localeStr.contains("-")) {
            String[] localArr = localeStr.split("-");
            this.locale = new Locale(localArr[0], localArr[1]);
        } else if (localeStr.contains("_")) {
            String[] localArr = localeStr.split("_");
            this.locale = new Locale(localArr[0], localArr[1]);
        } else {
            this.locale = new Locale(localeStr);
        }
        setName(locale.toString());
    }

    /**
     * Use other constructor instead
     */
    public Language () {

    }

    public Language(String iso639_2, String languageName) {
        setLocale(iso639_2);
    }
}
