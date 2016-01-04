package co.mide.translator;

import java.util.ArrayList;

/**
 * Class used in gson for parsing the json response from google.
 * Created by Olumide on 1/1/2016.
 */
public class TranslateResult {
    public TranslationDataResult data;
}

class TranslationDataResult {
    public ArrayList<Translation> translations = new ArrayList<>();
}

class Translation {
    public String translatedText;
    public String detectedSourceLanguage;
}
