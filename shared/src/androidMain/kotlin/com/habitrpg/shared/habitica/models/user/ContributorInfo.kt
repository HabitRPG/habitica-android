package com.habitrpg.shared.habitica.models.user

import android.util.SparseIntArray

import com.habitrpg.android.habitica.R
import com.habitrpg.shared.habitica.models.user.User

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ContributorInfo : RealmObject() {

    @PrimaryKey
    var userId: String? = null

    var user: User? = null
    private var admin: Boolean = false
    var contributions: String? = null
    var level: Int = 0
    var text: String? = null

    val contributorColor: Int
        get() {
            var rColor = android.R.color.black


            if (CONTRIBUTOR_COLOR_DICT.get(this.level, -1) > 0) {
                rColor = CONTRIBUTOR_COLOR_DICT.get(this.level)
            }

            return rColor
        }

    val contributorForegroundColor: Int
        get() = android.R.color.white

    fun getAdmin(): Boolean? {
        return this.admin
    }

    fun setAdmin(admin: Boolean?) {
        this.admin = admin!!
    }

    companion object {

        val CONTRIBUTOR_COLOR_DICT: SparseIntArray

        init {
            CONTRIBUTOR_COLOR_DICT = SparseIntArray()
            CONTRIBUTOR_COLOR_DICT.put(0, R.color.contributor_0)
            CONTRIBUTOR_COLOR_DICT.put(1, R.color.contributor_1)
            CONTRIBUTOR_COLOR_DICT.put(2, R.color.contributor_2)
            CONTRIBUTOR_COLOR_DICT.put(3, R.color.contributor_3)
            CONTRIBUTOR_COLOR_DICT.put(4, R.color.contributor_4)
            CONTRIBUTOR_COLOR_DICT.put(5, R.color.contributor_5)
            CONTRIBUTOR_COLOR_DICT.put(6, R.color.contributor_6)
            CONTRIBUTOR_COLOR_DICT.put(7, R.color.contributor_7)
            CONTRIBUTOR_COLOR_DICT.put(8, R.color.contributor_mod)
            CONTRIBUTOR_COLOR_DICT.put(9, R.color.contributor_staff)
        }
    }
}
