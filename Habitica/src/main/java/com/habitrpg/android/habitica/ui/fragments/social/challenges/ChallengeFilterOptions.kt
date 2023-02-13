package com.habitrpg.android.habitica.ui.fragments.social.challenges

import com.habitrpg.android.habitica.models.social.Group

data class ChallengeFilterOptions(
    var showByGroups: List<Group>,
    var showOwned: Boolean = false,
    var notOwned: Boolean = false
)
