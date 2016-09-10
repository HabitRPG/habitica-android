package com.habitrpg.android.habitica.helpers;

import java.util.Calendar;

/**
 * Created by DanielKaparunakis on 9/10/16.
 */
public class FirstDayOfTheWeekHelper {

    private int firstDayOfTheWeek;

    public FirstDayOfTheWeekHelper(String firstDayOfTheWeekSharedPref) {
        switch (firstDayOfTheWeekSharedPref){
            case "1":
                firstDayOfTheWeek = Calendar.SUNDAY;
                break;
            case "2":
                firstDayOfTheWeek = Calendar.MONDAY;
                break;
            case "3":
                firstDayOfTheWeek = Calendar.TUESDAY;
                break;
            case "4":
                firstDayOfTheWeek = Calendar.WEDNESDAY;
                break;
            case "5":
                firstDayOfTheWeek = Calendar.THURSDAY;
                break;
            case "6":
                firstDayOfTheWeek = Calendar.FRIDAY;
                break;
            case "7":
                firstDayOfTheWeek = Calendar.SATURDAY;
                break;
        }
    }

    public int getFirstDayOfTheWeek() {
        return firstDayOfTheWeek;
    }
}
