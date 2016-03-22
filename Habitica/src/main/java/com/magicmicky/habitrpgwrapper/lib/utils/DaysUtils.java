package com.magicmicky.habitrpgwrapper.lib.utils;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Days;

/**
 * Created by magicmicky on 04/02/15.
 */



public class DaysUtils {
    public static Days getDaysFromBooleans(boolean[] b) {
        Days d = new Days();
        d.setM(b[0]);
        d.setT(b[1]);
        d.setW(b[2]);
        d.setTh(b[3]);
        d.setF(b[4]);
        d.setS(b[5]);
        d.setSu(b[6]);
        return d;
    }
    public static boolean[] getBooleansFromDays(Days days) {
        boolean[] b = new boolean[7];
        b[0] = days.getM();
        b[1] = days.getT();
        b[2] = days.getW();
        b[3] = days.getTh();
        b[4] = days.getF();
        b[5] = days.getS();
        b[6] = days.getSu();
        return b;
    }
}

