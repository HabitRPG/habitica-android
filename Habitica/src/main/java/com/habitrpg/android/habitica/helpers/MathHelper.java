package com.habitrpg.android.habitica.helpers;

public class MathHelper {
    static public Double round(Double value, int n) {
        return (Math.round(value * Math.pow(10, n))) / (Math.pow(10, n));
    }
}
