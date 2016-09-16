package com.habitrpg.android.habitica.helpers;

import java.util.Calendar;

/**
 * Created by DanielKaparunakis on 9/10/16.
 */
public class FirstDayOfTheWeekHelper {

    private int firstDayOfTheWeek;
    private int dailyTaskFormOffset;

    public FirstDayOfTheWeekHelper(String firstDayOfTheWeekSharedPref) {
        switch (firstDayOfTheWeekSharedPref){
            case "1":
                firstDayOfTheWeek = Calendar.SUNDAY;
                dailyTaskFormOffset = 1;
                break;
            case "2":
                firstDayOfTheWeek = Calendar.MONDAY;
                dailyTaskFormOffset = 0;
                break;
            case "3":
                firstDayOfTheWeek = Calendar.TUESDAY;
                dailyTaskFormOffset = 6;
                break;
            case "4":
                firstDayOfTheWeek = Calendar.WEDNESDAY;
                dailyTaskFormOffset = 5;
                break;
            case "5":
                firstDayOfTheWeek = Calendar.THURSDAY;
                dailyTaskFormOffset = 4;
                break;
            case "6":
                firstDayOfTheWeek = Calendar.FRIDAY;
                dailyTaskFormOffset = 3;
                break;
            case "7":
                firstDayOfTheWeek = Calendar.SATURDAY;
                dailyTaskFormOffset = 2;
                break;
        }
    }

    public int getFirstDayOfTheWeek() {
        return firstDayOfTheWeek;
    }

    public int getDailyTaskFormOffset() {
        return dailyTaskFormOffset;
    }
}
