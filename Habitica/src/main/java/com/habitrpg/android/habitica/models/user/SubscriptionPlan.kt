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
    var gemsBought: Int? = null
    var extraMonths: Int? = null
    var quantity: Int? = null
    var consecutive: SubscriptionPlanConsecutive? = null
    var mysteryItemCount = 0
    var additionalData: AdditionalSubscriptionInfo? = null

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

    val monthsUntilNextHourglass: Int?
        get() {
            var renewalUntilNextHourglass = 0
            if (planId != null && dateTerminated == null && consecutive?.count != null) {
                when (planId) {
                    // If user has a initial basic monthly subscription, receive hourglasses on fourth month, else receive on third month.
                    PLANID_BASIC -> renewalUntilNextHourglass = if (consecutive?.count!! < 3) { 4 } else 3
                    PLANID_BASICEARNED -> renewalUntilNextHourglass = if (consecutive?.count!! < 3) { 4 } else 3
                    PLANID_BASIC3MONTH -> renewalUntilNextHourglass = 3
                    PLANID_BASIC6MONTH -> renewalUntilNextHourglass = 6
                    PLANID_BASIC12MONTH -> renewalUntilNextHourglass = 12
                }
                return renewalUntilNextHourglass - (consecutive?.count!! % renewalUntilNextHourglass)
            }
            return null
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
