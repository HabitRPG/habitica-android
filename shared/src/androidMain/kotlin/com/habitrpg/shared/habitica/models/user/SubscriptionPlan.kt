package com.habitrpg.shared.habitica.models.user

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class SubscriptionPlan : SubscriptionPlanConsts, RealmObject() {

    @PrimaryKey
    actual var customerId: String? = null
        set(customerId: String?) {
            field = customerId
            if (consecutive != null && !consecutive!!.isManaged) {
                consecutive!!.customerId = customerId
            }
        }
    actual var dateCreated: Date? = null
    actual var dateUpdated: Date? = null
    actual var dateTerminated: Date? = null
    actual var paymentMethod: String? = null
    actual var planId: String? = null
    actual var gemsBought: Int? = null
    actual var extraMonths: Int? = null
    actual var quantity: Int? = null
    actual var consecutive: SubscriptionPlanConsecutive? = null

    actual var mysteryItemCount: Int = 0

    actual val isActive: Boolean
        get() {
            val today = Date()
            return customerId != null && (this.dateTerminated == null || this.dateTerminated!!.after(today))
        }

    actual fun totalNumberOfGems(): Int {
        return if (customerId == null || consecutive == null) {
            0
        } else 25 + consecutive!!.gemCapExtra
    }

    actual fun numberOfGemsLeft(): Int {
        return totalNumberOfGems() - gemsBought!!
    }
}
