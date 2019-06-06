package com.habitrpg.android.habitica.models.inventory


import android.content.Context
import com.habitrpg.android.habitica.R
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SpecialItem : RealmObject(), Item {

    @PrimaryKey
    internal var key: String = ""
    internal var text: String = ""
    internal var notes: String = ""
    internal var value: Int? = null
    var isMysteryItem: Boolean = false

    override fun getType(): String {
        return "special"
    }

    override fun getKey(): String {
        return key
    }

    override fun getText(): String {
        return text
    }

    override fun getValue(): Int? {
        return value
    }

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
