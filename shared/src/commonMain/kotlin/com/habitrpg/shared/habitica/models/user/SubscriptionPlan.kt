package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate



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
