package com.habitrpg.android.habitica.helpers;

import java.util.Locale;

/**
 * Created by DanielKaparunakis on 9/2/16.
 */
public class LanguageHelper {

    private Locale locale = null;
    private String languageCode = null;

    public LanguageHelper(String languageSharedPref) {

        switch (languageSharedPref) {
            case "iw":
                locale = new Locale("iw");
                languageCode = "he";
                break;
            case "hr":
                locale = new Locale("hr", "HR");
                languageCode = "hr";
                break;
            case "in":
                locale = new Locale("in");
                languageCode = "id";
                break;
            case "pt":
                locale = new Locale("pt", "PT");
                languageCode = "pt";
                break;
            default:
                if (languageSharedPref.contains("_")) {
                    String[] languageCodeParts = languageSharedPref.split("_");
                    locale = new Locale(languageCodeParts[0], languageCodeParts[1]);
                } else {
                    locale = new Locale(languageSharedPref);
                }
                languageCode = languageSharedPref;
                break;
        }
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public Locale getLocale() {
        return locale;
    }
}
