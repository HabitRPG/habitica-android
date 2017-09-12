package com.habitrpg.android.habitica.helpers;

import android.content.Context;

import com.habitrpg.android.habitica.R;

import java.text.DecimalFormat;

public class NumberAbbreviator {

    public static String abbreviate(Context context, double number) {
        int counter = 0;
        while (number >= 1000) {
            counter++;
            number = number / 1000;
        }

        DecimalFormat formatter = new DecimalFormat("###.##"+abbreviationForCounter(context, counter));
        return formatter.format(number);
    }


    private static String abbreviationForCounter(Context context, int counter) {
        switch (counter) {
            case 0:
                return "";
            case 1:
                return context.getString(R.string.thousand_abbrev);
            case 2:
                return context.getString(R.string.million_abbrev);
            case 3:
                return context.getString(R.string.billion_abbrev);
            default:
                return "";
        }
    }

}
