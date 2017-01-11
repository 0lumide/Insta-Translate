package co.mide.instatranslate;

import co.mide.translator.Language;

public class LanguagePair{
    private Language sourceLanguage, destLanguage;

    public LanguagePair(Language source, Language dest){
        sourceLanguage = source;
        destLanguage = dest;
    }

    public Language getSourceLanguage(){
        return sourceLanguage;
    }

    public  Language getDestLanguage(){
        return destLanguage;
    }
}
