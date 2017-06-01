package com.habitrpg.android.habitica.models.responses;

import com.habitrpg.android.habitica.models.user.Items;

import java.util.HashMap;

public class BuyResponse {

    public Items items;

    public HashMap<String, String> armoire;

    public Integer lvl;
    public Double gp, exp, mp, hp;

}
