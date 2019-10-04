package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativeLibraries.NativeSparseIntArray

expect open class ContributorInfo  {

    var userId: String?

    var user: User?
    var admin: Boolean
    var contributions: String?
    var level: Int
    var text: String?

    val contributorColor: Int

    val contributorForegroundColor: Int

    companion object {
        val CONTRIBUTOR_COLOR_DICT: NativeSparseIntArray
    }
}
