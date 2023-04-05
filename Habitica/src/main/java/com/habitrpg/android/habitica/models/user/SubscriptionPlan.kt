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
    var perkMonthCount: Int = 0

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
            return if (isActive) {
                25 + (consecutive?.gemCapExtra ?: 0)
            } else 0
        }

    val numberOfGemsLeft: Int
        get() {
            return totalNumberOfGems - (gemsBought ?: 0)
        }

    /*
      If user has a initial basic monthly subscription, receive hourglasses on fourth month,
      else receive on third month (subtract 1 from total consecutive count)
     */

    val subMonthCount: Int
        get() {
            return when (planId) {
                "basic_earned" -> 1
                "basic_3mo" -> 3
                "basic_6mo" -> 6
                "google_6mo" -> 6
                "basic_12mo" -> 12
                "group_plan_auto" -> 1
                else -> 0
            }
        }

    val monthsUntilNextHourglass: Int
        get() {
            return if (subMonthCount > 1) {
                (consecutive?.offset ?: 0) + 1
            } else {
                (3 - perkMonthCount)
            }
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
