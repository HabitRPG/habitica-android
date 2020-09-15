package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeColor
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.NativeSparseIntArray
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class ContributorInfo : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    var user: User? = null
    var admin: Boolean = false
    var contributions: String? = null
    var level: Int = 0
    var text: String? = null

    val contributorColor: Int
        get() {
            var rColor = NativeColor.black


            if (CONTRIBUTOR_COLOR_DICT.get(this.level, -1) > 0) {
                rColor = CONTRIBUTOR_COLOR_DICT.get(this.level, -1)
            }

            return rColor
        }

    val contributorForegroundColor: Int
        get() = NativeColor.white

    fun getAdmin(): Boolean? {
        return this.admin
    }

    fun setAdmin(admin: Boolean?) {
        this.admin = admin!!
    }

    companion object {

        val CONTRIBUTOR_COLOR_DICT: NativeSparseIntArray

        init {
            CONTRIBUTOR_COLOR_DICT = NativeSparseIntArray()
            CONTRIBUTOR_COLOR_DICT.put(0, NativeColor.contributor_0)
            CONTRIBUTOR_COLOR_DICT.put(1, NativeColor.contributor_1)
            CONTRIBUTOR_COLOR_DICT.put(2, NativeColor.contributor_2)
            CONTRIBUTOR_COLOR_DICT.put(3, NativeColor.contributor_3)
            CONTRIBUTOR_COLOR_DICT.put(4, NativeColor.contributor_4)
            CONTRIBUTOR_COLOR_DICT.put(5, NativeColor.contributor_5)
            CONTRIBUTOR_COLOR_DICT.put(6, NativeColor.contributor_6)
            CONTRIBUTOR_COLOR_DICT.put(7, NativeColor.contributor_7)
            CONTRIBUTOR_COLOR_DICT.put(8, NativeColor.contributor_mod)
            CONTRIBUTOR_COLOR_DICT.put(9, NativeColor.contributor_staff)
        }
    }
}
