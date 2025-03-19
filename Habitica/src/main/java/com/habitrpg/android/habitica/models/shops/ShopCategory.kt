package com.habitrpg.android.habitica.models.shops

import com.google.gson.annotations.SerializedName
import io.realm.RealmList
import java.util.Date

class ShopCategory {
    var identifier: String = ""
    var text: String = ""
    var notes: String = ""
    var path: String = ""
    var purchaseAll: Boolean? = null
    var pinType: String = ""

    @SerializedName("end")
    var endDate: Date? = null

    var itemEndDates: MutableList<Date> = ArrayList()

    var items: MutableList<ShopItem> = ArrayList()

    val endDates: Set<Date>
        get() {
            val dates = itemEndDates.distinct().toMutableSet()
            if (items.isNotEmpty()) {
                dates.addAll(items.mapNotNull { it.endDate })
            }
            endDate?.let { dates.add(it) }
            return dates
        }
}
