package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.helpers.HabiticaProduct
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import java.util.Date
import kotlin.math.max

@RealmClass(embedded = true)
open class SubscriptionPlan :
    RealmObject(),
    BaseObject {
    var customerId: String? = null
    var dateCreated: Date? = null
    var dateUpdated: Date? = null

    @JvmField
    var dateTerminated: Date? = null
    var paymentMethod: String? = null

    @JvmField
    var planId: String? = null
    var deferredPlanId: String? = null
    var active: Boolean? = null
    var gemsBought: Int? = null
    var extraMonths: Int? = null
    var quantity: Int? = null
    var consecutive: SubscriptionPlanConsecutive? = null
    var cumulativeCount: Int? = null
    var deferred: SubscriptionPlanDeferred? = null
    var mysteryItemCount = 0
    var additionalData: AdditionalSubscriptionInfo? = null
    var hourglassPromoReceived: Date? = null

    var nextBillingDate: Date? = null
    var nextPaymentProcessing: Date? = null

    @SerializedName("owner")
    var ownerID: String? = null
    val isGroupPlanSub: Boolean
        get() = customerId == "group-plan" || paymentMethod == "Group Plan"
    val isGiftedSub: Boolean
        get() = customerId == "Gift" || paymentMethod == "Gift"
    val isActive: Boolean
        get() {
            val today = Date()
            return customerId != null && (dateTerminated == null || dateTerminated!!.after(today) || active == true)
        }
    val isTerminated: Boolean
        get() = dateTerminated != null

    val totalNumberOfGemsAlways: Int
        get() {
            return 24 + (consecutive?.gemCapExtra ?: 0)
        }

    val totalNumberOfGems: Int
        get() {
            if (!isActive) return 0
            return 24 + (consecutive?.gemCapExtra ?: 0)
        }

    val numberOfGemsLeft: Int
        get() {
            if (!isActive) return 0
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

    val monthsSubscribed: Int
        get() {
            if (!isActive) return 0
            return max((consecutive?.count ?: 0), (cumulativeCount ?: 0))
        }

    val habiticaProduct: HabiticaProduct?
        get() =
            when (planId) {
                "basic_earned" -> HabiticaProduct.SUBSCRIPTION_1_MONTH
                "basic_3mo" -> HabiticaProduct.SUBSCRIPTION_3_MONTH
                "basic_6mo" -> HabiticaProduct.SUBSCRIPTION_6_MONTH
                "basic_12mo" -> HabiticaProduct.SUBSCRIPTION_12_MONTH
                else -> null
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
