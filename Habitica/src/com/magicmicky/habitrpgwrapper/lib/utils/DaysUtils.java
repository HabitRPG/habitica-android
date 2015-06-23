package com.magicmicky.habitrpgwrapper.lib.utils;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Daily;
/**
 * Created by magicmicky on 04/02/15.
 */



public class DaysUtils {
    public static Daily.Days getDaysFromBooleans(boolean[] b) {
        Daily.Days d = new Daily.Days();
        d.setM(b[0]);
        d.setT(b[1]);
        d.setW(b[2]);
        d.setTh(b[3]);
        d.setF(b[4]);
        d.setS(b[5]);
        d.setSu(b[6]);
        return d;
    }
    public static boolean[] getBooleansFromDays(Daily.Days days) {
        boolean[] b = new boolean[7];
        b[0] = days.isM();
        b[1] = days.isT();
        b[2] = days.isW();
        b[3] = days.isTh();
        b[4] = days.isF();
        b[5] = days.isS();
        b[6] = days.isSu();
        return b;
    }
}

