package com.habitrpg.android.habitica.helpers;

import java.util.Locale;

/**
 * Created by DanielKaparunakis on 9/2/16.
 */
public class LanguageHelper {

    private Locale locale = null;
    private String languageCode = null;
    private boolean languageAvailable = false;

    public LanguageHelper (String languagePreference) {
        switch (languagePreference){
            case "en":
                locale = new Locale("en");
                languageCode = "en";
                languageAvailable = true;
                break;
            case "bg":
                locale = new Locale("bg");
                languageCode = "bg";
                languageAvailable = true;
                break;
            case "de":
                locale = new Locale("de");
                languageCode = "de";
                languageAvailable = true;
                break;
            case "en-rGB":
                locale = new Locale("en", "GB");
                languageCode = "en_GB";
                languageAvailable = true;
                break;
            case "es":
                locale = new Locale("es");
                languageCode = "es";
                languageAvailable = true;
                break;
            case "fr":
                locale = new Locale("fr");
                languageCode = "fr";
                languageAvailable = true;
                break;
            case "iw":
                locale = new Locale("iw");
                languageCode = "he";
                languageAvailable = true;
                break;
            case "hr":
                locale = new Locale("hr", "HR");
                languageCode = "hu";
                languageAvailable = true;
                break;
            case "in":
                locale = new Locale("in");
                languageCode = "id";
                languageAvailable = true;
                break;
            case "it":
                locale = new Locale("it");
                languageCode = "it";
                languageAvailable = true;
                break;
            case "ja":
                locale = new Locale("ja");
                languageCode = "ja";
                languageAvailable = true;
                break;
            case "nl":
                locale = new Locale("nl");
                languageCode = "nl";
                languageAvailable = true;
                break;
            case "pl":
                locale = new Locale("pl");
                languageCode = "pl";
                languageAvailable = true;
                break;
            case "pt":
                locale = new Locale("pt","PT");
                languageCode = "pt";
                languageAvailable = true;
                break;
            case "pt-rBR":
                locale = new Locale("pt","BR");
                languageCode = "pt_BR";
                languageAvailable = true;
                break;
            case "ru":
                locale = new Locale("ru");
                languageCode = "ru";
                languageAvailable = true;
                break;
            case "zh":
                locale = new Locale("zh");
                languageCode = "zh";
                languageAvailable = true;
                break;
            case "zh-rTW":
                locale = new Locale("zh","TW");
                languageCode = "zh_TW";
                languageAvailable = true;
                break;
        }
    }

    public boolean isLanguageAvailable() {
        return languageAvailable;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public Locale getLocale() {
        return locale;
    }
}
