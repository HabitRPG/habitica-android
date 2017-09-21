package com.habitrpg.android.habitica.models.shops;

import com.habitrpg.android.habitica.R;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ShopItemUnlockCondition extends RealmObject {

    @PrimaryKey
    String condition;

    public int readableUnlockConditionId() {
        switch (this.condition) {
            case "party invite":
                return R.string.party_invite;
            case "login incentive":
                return R.string.login_incentive;
            case "create account":
                return R.string.create_account;
            default:
                return R.string.empty;
        }
    }
}
