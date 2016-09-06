package com.habitrpg.android.habitica.helpers;

import java.util.Locale;

/**
 * Created by DanielKaparunakis on 9/2/16.
 */
public class LanguageHelper {

    private Locale locale = null;
    private String languageCode = null;
    private boolean languageAvailable = false;


    public LanguageHelper (String languageSharedPref) {

        switch (languageSharedPref){
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
            case "pt":
                locale = new Locale("pt","PT");
                languageCode = "pt";
                languageAvailable = true;
                break;
            default:
                if (languageSharedPref.contains("_")) {
                    String[] languageCodeParts = languageSharedPref.split("_");
                    locale = new Locale(languageCodeParts[0],languageCodeParts[1]);
                } else {
                    locale = new Locale(languageSharedPref);
                }
                languageCode = languageSharedPref;
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
