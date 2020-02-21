package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation


open class SubscriptionPlan : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var customerId: String? = null
    var dateCreated: NativeDate? = null
    var dateUpdated: NativeDate? = null
    var dateTerminated: NativeDate? = null
    var paymentMethod: String? = null
    var planId: String? = null
    var gemsBought: Int? = null
    var extraMonths: Int? = null
    var quantity: Int? = null
    var consecutive: SubscriptionPlanConsecutive? = null

    var mysteryItemCount: Int = 0
    @SerializedNameAnnotation("owner")
    var ownerID: String? = null

    val isGroupPlanSub: Boolean
        get() = customerId == "group-plan"

    val isGiftedSub: Boolean
        get() = customerId == "Gift"

    val isActive: Boolean
        get() {
            val today = NativeDate()
            return customerId != null && (this.dateTerminated == null || this.dateTerminated!!.after(today))
        }

    fun totalNumberOfGems(): Int {
        return if (customerId == null || consecutive == null) {
            0
        } else 25 + consecutive!!.gemCapExtra
    }

    fun numberOfGemsLeft(): Int {
        return totalNumberOfGems() - gemsBought!!
    }


    fun setCustomerId(customerId: String) {
        this.customerId = customerId
        if (consecutive != null && !consecutive!!.isManaged()) {
            consecutive!!.customerId = customerId
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
