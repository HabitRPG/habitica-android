package com.habitrpg.android.habitica.models.shops

import com.google.gson.annotations.SerializedName
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

    var items: MutableList<ShopItem> = ArrayList()
}
