package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.R;

public class ShopItemUnlockCondition {

    String condition;

    public int readableUnlockConditionId() {
        switch (this.condition) {

            case "party invite":
                return R.string.party_invite;
            default:
                return 0;
        }
    }
}
