package com.magicmicky.habitrpgwrapper.lib.models.responses;

import com.magicmicky.habitrpgwrapper.lib.models.Flags;
import com.magicmicky.habitrpgwrapper.lib.models.Items;
import com.magicmicky.habitrpgwrapper.lib.models.Stats;

import java.util.HashMap;

public class BuyResponse {

    public Items items;

    public HashMap<String, String> armoire;

    public Integer lvl;
    public Double gp, exp, mp, hp;

}
