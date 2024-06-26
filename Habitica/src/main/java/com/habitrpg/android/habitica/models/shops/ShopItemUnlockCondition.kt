package com.habitrpg.android.habitica.models.shops

import android.content.Context
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class ShopItemUnlockCondition : RealmObject(), BaseObject {
    private var condition: String? = null
    var incentiveThreshold: Int? = null

    fun readableUnlockCondition(context: Context): String =
        when (this.condition) {
            "party invite" -> context.getString(R.string.party_invite)
            "login reward" ->
                if (incentiveThreshold != null) {
                    context.getString(
                        R.string.login_incentive_count,
                        incentiveThreshold
                    )
                } else {
                    context.getString(R.string.login_incentive)
                }

            "create account" -> context.getString(R.string.create_account)
            else -> ""
        }

    fun shortReadableUnlockCondition(context: Context): String =
        when (this.condition) {
            "party invite" -> context.getString(R.string.party_invite_short)
            "login reward" ->
                if (incentiveThreshold != null) {
                    context.getString(
                        R.string.login_incentive_short_count,
                        incentiveThreshold
                    )
                } else {
                    context.getString(R.string.login_incentive_short)
                }

            "create account" -> context.getString(R.string.create_account_short)
            else -> ""
        }
}
