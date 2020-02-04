package com.habitrpg.android.habitica.models.shops

import com.habitrpg.android.habitica.R

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ShopItemUnlockCondition : RealmObject() {

    @PrimaryKey
    var questKey: String? = null
    private var condition: String? = null

    fun readableUnlockConditionId(): Int = when (this.condition) {
        "party invite" -> R.string.party_invite
        "login incentive" -> R.string.login_incentive
        "create account" -> R.string.create_account
        else -> R.string.empty
    }

    fun shortReadableUnlockConditionId(): Int = when (this.condition) {
        "party invite" -> R.string.party_invite_short
        "login incentive" -> R.string.login_incentive_short
        "create account" -> R.string.create_account_short
        else -> R.string.empty
    }
}
