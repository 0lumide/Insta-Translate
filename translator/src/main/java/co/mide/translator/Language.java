package co.mide.translator;

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

    public Language () {

    }

    public Language(String iso639_2, String languageName) {
        this.language = iso639_2;
        this.name = languageName;
    }
}
