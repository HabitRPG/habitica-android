@file:UseSerializers(DateSerializer::class)

package com.habitrpg.android.habitica.models.shops

import com.habitrpg.shared.habitica.serialization.DateSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@Serializable
class ShopCategory {
    var identifier: String = ""
    var text: String = ""
    var notes: String = ""
    var path: String = ""
    var purchaseAll: Boolean? = null
    var pinType: String = ""

    @SerialName("end")
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
