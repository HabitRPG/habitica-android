package com.habitrpg.android.habitica.models.shops

import com.habitrpg.android.habitica.R

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ShopItemUnlockCondition : RealmObject() {

    @PrimaryKey
    internal var condition: String? = null

    fun readableUnlockConditionId(): Int = when (this.condition) {
        "party invite" -> R.string.party_invite
        "login incentive" -> R.string.login_incentive
        "create account" -> R.string.create_account
        else -> R.string.empty
    }
}
