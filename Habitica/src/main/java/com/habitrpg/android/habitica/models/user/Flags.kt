package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.TutorialStep
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Flags : RealmObject(), BaseObject, com.habitrpg.common.habitica.models.AvatarFlags {
    var tutorial: RealmList<TutorialStep>? = null
    var showTour = false
    var dropsEnabled = false
    var itemsEnabled = false
    var newStuff = false
    var lastNewStuffRead: String? = null
    override var classSelected = false
    var rebirthEnabled = false
    var welcomed = false
    var armoireEnabled = false
    var armoireOpened = false
    var armoireEmpty = false
    var communityGuidelinesAccepted = false
    var verifiedUsername = false
    var isWarnedLowHealth = false
}
