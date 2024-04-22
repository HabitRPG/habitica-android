package com.habitrpg.android.habitica.models.inventory

import android.content.Context
import com.habitrpg.android.habitica.R
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SpecialItem : RealmObject(), Item {
    override val type: String
        get() = "special"

    @PrimaryKey
    override var key: String = ""
    override var text: String = ""
    internal var notes: String = ""
    override var value: Int = 0
    override var event: ItemEvent? = null
    var target: String? = null
    var isMysteryItem: Boolean = false

    companion object {
        fun makeMysteryItem(context: Context): SpecialItem {
            val item = SpecialItem()
            item.text = context.getString(R.string.mystery_item)
            item.notes = context.getString(R.string.myster_item_notes)
            item.key = "inventory_present"
            item.isMysteryItem = true
            return item
        }
    }
}
