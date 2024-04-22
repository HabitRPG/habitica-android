package com.habitrpg.android.habitica.models.inventory

import android.graphics.Color
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by phillip on 31.01.18.
 */

open class QuestColors : RealmObject(), BaseObject {
    @PrimaryKey
    var key: String? = null
    var dark: String? = null
    var medium: String? = null
    var light: String? = null
    var extralight: String? = null

    val darkColor: Int
        get() {
            return Color.parseColor(dark)
        }
    val mediumColor: Int
        get() {
            return Color.parseColor(medium)
        }
    val lightColor: Int
        get() {
            return Color.parseColor(light)
        }
    val extraLightColor: Int
        get() {
            return Color.parseColor(extralight)
        }
}
