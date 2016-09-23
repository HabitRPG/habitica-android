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

    public static FirstDayOfTheWeekHelper newInstance(int firstDayOfTheWeekSharedPref) {
        switch (firstDayOfTheWeekSharedPref){
            case Calendar.SUNDAY:
                return new FirstDayOfTheWeekHelper(1, Calendar.SUNDAY);
            case Calendar.MONDAY:
                return new FirstDayOfTheWeekHelper(0, Calendar.MONDAY);
            case Calendar.TUESDAY:
                return new FirstDayOfTheWeekHelper(6, Calendar.TUESDAY);
            case Calendar.WEDNESDAY:
                return new FirstDayOfTheWeekHelper(5, Calendar.WEDNESDAY);
            case Calendar.THURSDAY:
                return new FirstDayOfTheWeekHelper(4, Calendar.THURSDAY);
            case Calendar.FRIDAY:
                return new FirstDayOfTheWeekHelper(3, Calendar.FRIDAY);
            case Calendar.SATURDAY:
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
