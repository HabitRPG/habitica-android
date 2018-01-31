package com.habitrpg.android.habitica.models.inventory

import android.graphics.Color
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by phillip on 31.01.18.
 */

open class QuestColors : RealmObject() {

    @PrimaryKey
    var key: String? = null
    var dark: String? = null
    var medium: String? = null
    var light: String? = null
    var extralight: String? = null

    var darkColor: Int = 0
        get() {
            return Color.parseColor(dark)
        }
    var mediumColor: Int = 0
        get() {
            return Color.parseColor(medium)
        }
    var lightColor: Int = 0
        get() {
            return Color.parseColor(light)
        }
    var extraLightColor: Int = 0
        get() {
            return Color.parseColor(extralight)
        }
}
