package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.TutorialStep
import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper

expect class Flags  {
    var userId: String?

    internal var tutorial: RealmListWrapper<TutorialStep>?
    var showTour: Boolean
    var dropsEnabled: Boolean
    var itemsEnabled: Boolean
    var newStuff: Boolean
    var classSelected: Boolean
    var rebirthEnabled: Boolean
    var welcomed: Boolean
    var armoireEnabled: Boolean
    var armoireOpened: Boolean
    var armoireEmpty: Boolean
    var isCommunityGuidelinesAccepted: Boolean
    var isVerifiedUsername: Boolean
    var isWarnedLowHealth: Boolean
}
