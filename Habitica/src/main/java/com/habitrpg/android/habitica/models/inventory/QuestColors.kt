package com.habitrpg.android.habitica.models.inventory

import androidx.core.graphics.toColorInt
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
            return dark.toColorInt()
        }
    val mediumColor: Int
        get() {
            return medium.toColorInt()
        }
    val lightColor: Int
        get() {
            return light.toColorInt()
        }
    val extraLightColor: Int
        get() {
            return extralight.toColorInt()
        }
}
