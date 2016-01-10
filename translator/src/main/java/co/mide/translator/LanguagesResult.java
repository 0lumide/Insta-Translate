package co.mide.translator;

import java.util.ArrayList;

/**
 * Class used in gson for parsing the json response from google.
 * Created by Olumide on 1/5/2016.
 */
public class LanguagesResult {
    public LanguagesDataResult data;
}

class LanguagesDataResult {
    public ArrayList<Language> languages = new ArrayList<>();
}