package com.habitrpg.android.habitica.helpers;

import java.util.Calendar;

/**
 * Created by DanielKaparunakis on 9/10/16.
 */
public class FirstDayOfTheWeekHelper {

    private int firstDayOfTheWeek;
    private int dailyTaskFormOffset;

    private FirstDayOfTheWeekHelper(int dailyTaskFormOffset, int firstDayOfTheWeek) {
        this.dailyTaskFormOffset = dailyTaskFormOffset;
        this.firstDayOfTheWeek = firstDayOfTheWeek;
    }

    public static FirstDayOfTheWeekHelper newInstance(String firstDayOfTheWeekSharedPref) {
        switch (firstDayOfTheWeekSharedPref){
            case "1":
                return new FirstDayOfTheWeekHelper(1, Calendar.SUNDAY);
            case "2":
                return new FirstDayOfTheWeekHelper(0, Calendar.MONDAY);
            case "3":
                return new FirstDayOfTheWeekHelper(6, Calendar.TUESDAY);
            case "4":
                return new FirstDayOfTheWeekHelper(5, Calendar.WEDNESDAY);
            case "5":
                return new FirstDayOfTheWeekHelper(4, Calendar.THURSDAY);
            case "6":
                return new FirstDayOfTheWeekHelper(3, Calendar.FRIDAY);
            case "7":
                return new FirstDayOfTheWeekHelper(2, Calendar.SATURDAY);
            default:
                return new FirstDayOfTheWeekHelper(1, Calendar.SUNDAY);
        }
    }

    public int getFirstDayOfTheWeek() {
        return firstDayOfTheWeek;
    }

    public int getDailyTaskFormOffset() {
        return dailyTaskFormOffset;
    }
}
