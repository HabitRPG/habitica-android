package com.habitrpg.android.habitica.ui.helpers;


import com.habitrpg.android.habitica.R;

public class HabitColorHelper {

    /**
     * Get the color resources depending on a certain score
     *
     * @param d the score
     * @return the color resource id
     */
    public static int GetItemColorByValue(double d)
    {
        if (d < -20)
            return R.color.worst;
        if (d < -10)
            return R.color.worse;
        if (d < -1)
            return R.color.bad;
        if (d < 5)
            return R.color.neutral;
        if (d < 10)
            return R.color.better;
        return R.color.best;
    }

    /**
     * Get the button color resources depending on a certain score
     *
     * @param d the score
     * @return the color resource id
     */
    public static int GetItemButtonColorByValue(double d)
    {
        if (d < -20)
            return R.color.worst_btn;
        if (d < -10)
            return R.color.worse_btn;
        if (d < -1)
            return R.color.bad_btn;
        if (d < 5)
            return R.color.neutral_btn;
        if (d < 10)
            return R.color.better_btn;

        return R.color.best_btn;
    }
}
