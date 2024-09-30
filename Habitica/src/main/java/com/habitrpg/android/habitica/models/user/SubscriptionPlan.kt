package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.Date

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
    var active: Boolean? = null
    var gemsBought: Int? = null
    var extraMonths: Int? = null
    var quantity: Int? = null
    var consecutive: SubscriptionPlanConsecutive? = null
    var mysteryItemCount = 0
    var additionalData: AdditionalSubscriptionInfo? = null
    var hourglassPromoReceived: Date? = null

    @SerializedName("owner")
    var ownerID: String? = null
    val isGroupPlanSub: Boolean
        get() = customerId == "group-plan"
    val isGiftedSub: Boolean
        get() = customerId == "Gift"
    val isActive: Boolean
        get() {
            val today = Date()
            return customerId != null && (dateTerminated == null || dateTerminated!!.after(today) || active == true)
        }

    val totalNumberOfGems: Int
        get() {
            return 24 + (consecutive?.gemCapExtra ?: 0)
        }

    val numberOfGemsLeft: Int
        get() {
            return totalNumberOfGems - (gemsBought ?: 0)
        }


    val monthsUntilNextHourglass: Int
        get() {
            return 1
        }

    val isEligableForHourglassPromo: Boolean
        get() {
            return hourglassPromoReceived == null
        }

    companion object {
        const val PLANID_BASIC = "basic"
        const val PLANID_BASICEARNED = "basic_earned"
        const val PLANID_BASIC3MONTH = "basic_3mo"
        const val PLANID_BASIC6MONTH = "basic_6mo"
        const val PLANID_GOOGLE6MONTH = "google_6mo"
        const val PLANID_BASIC12MONTH = "basic_12mo"
    }
}
