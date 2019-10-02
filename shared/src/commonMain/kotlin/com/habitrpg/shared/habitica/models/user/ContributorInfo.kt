package com.habitrpg.shared.habitica.models.user

expect open class ContributorInfo  {

    var userId: String?

    var user: User?
    var admin: Boolean
    var contributions: String?
    var level: Int
    var text: String?

    val contributorColor: Int

    val contributorForegroundColor: Int

    fun getAdmin(): Boolean?

    fun setAdmin(admin: Boolean?)

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
