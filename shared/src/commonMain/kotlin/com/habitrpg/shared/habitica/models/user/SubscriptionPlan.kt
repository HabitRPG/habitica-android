package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate

class SubscriptionPlanConsts {
    companion object {
        var PLANID_BASIC = "basic"
        var PLANID_BASICEARNED = "basic_earned"
        var PLANID_BASIC3MONTH = "basic_3mo"
        var PLANID_BASIC6MONTH = "basic_6mo"
        var PLANID_GOOGLE6MONTH = "google_6mo"
        var PLANID_BASIC12MONTH = "basic_12mo"
    }
}

expect open class SubscriptionPlan {

    var customerId: String?
    var dateCreated: NativeDate?
    var dateUpdated: NativeDate?
    var dateTerminated: NativeDate?
    var paymentMethod: String?
    var planId: String?
    var gemsBought: Int?
    var extraMonths: Int?
    var quantity: Int?
    var consecutive: SubscriptionPlanConsecutive?

    var mysteryItemCount: Int

    val isActive: Boolean

    fun totalNumberOfGems(): Int

    fun numberOfGemsLeft(): Int
}
