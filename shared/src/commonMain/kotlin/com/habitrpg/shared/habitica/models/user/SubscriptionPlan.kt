package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation


open class SubscriptionPlan : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var customerId: String? = null
        set(customerId) {
            field = customerId
            val consecutive = this.consecutive
            if (consecutive != null && !consecutive.isManaged()) {
                consecutive.customerId = customerId
            }
        }
    var dateCreated: NativeDate = NativeDate()
    var dateUpdated: NativeDate = NativeDate()
    var dateTerminated: NativeDate? = null
    var paymentMethod: String? = null
    var planId: String? = null
    var gemsBought: Int = 0
    var extraMonths: Int = 0
    var quantity: Int = 0
    var consecutive: SubscriptionPlanConsecutive? = null

    var mysteryItemCount: Int = 0
    @SerializedNameAnnotation("owner")
    var ownerID: String = ""

    val isGroupPlanSub: Boolean
        get() = customerId == "group-plan"

    val isGiftedSub: Boolean
        get() = customerId == "Gift"

    val isActive: Boolean
        get() {
            val today = NativeDate()
            val dateTerminated = this.dateTerminated
            return customerId != null && (dateTerminated == null || dateTerminated.after(today))
        }

    fun totalNumberOfGems(): Int {
        return if (customerId == null || consecutive == null) {
            0
        } else 25 + consecutive!!.gemCapExtra
    }

    fun numberOfGemsLeft(): Int {
        return totalNumberOfGems() - gemsBought
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
