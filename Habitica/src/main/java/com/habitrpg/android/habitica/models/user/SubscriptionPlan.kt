package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass(embedded = true)
open class SubscriptionPlan : RealmObject(), BaseObject {
    var customerId: String? = null
    var dateCreated: Date? = null
    var dateUpdated: Date? = null
    @JvmField
    var dateTerminated: Date? = null
    var paymentMethod: String? = null
    @JvmField
    var planId: String? = null
    var gemsBought: Int? = null
    var extraMonths: Int? = null
    var quantity: Int? = null
    var consecutive: SubscriptionPlanConsecutive? = null
    var mysteryItemCount = 0

    @SerializedName("owner")
    var ownerID: String? = null
    val isGroupPlanSub: Boolean
        get() = customerId == "group-plan"
    val isGiftedSub: Boolean
        get() = customerId == "Gift"
    val isActive: Boolean
        get() {
            val today = Date()
            return customerId != null && (dateTerminated == null || dateTerminated!!.after(today))
        }

    fun totalNumberOfGems(): Int {
        return if (customerId == null || consecutive == null) {
            0
        } else 25 + consecutive!!.gemCapExtra
    }

    fun numberOfGemsLeft(): Int {
        return totalNumberOfGems() - gemsBought!!
    }

    companion object {
        var PLANID_BASIC = "basic"
        var PLANID_BASICEARNED = "basic_earned"
        var PLANID_BASIC3MONTH = "basic_3mo"
        var PLANID_BASIC6MONTH = "basic_6mo"
        var PLANID_GOOGLE6MONTH = "google_6mo"
        var PLANID_BASIC12MONTH = "basic_12mo"
    }
}