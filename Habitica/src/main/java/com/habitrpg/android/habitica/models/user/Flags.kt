@file:UseSerializers(DateSerializer::class, RealmListSerializer::class)

package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.utils.RealmListSerializer
import com.habitrpg.android.habitica.utils.TutorialStepListSerializer
import com.habitrpg.shared.habitica.models.AvatarFlags
import com.habitrpg.shared.habitica.serialization.DateSerializer
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@RealmClass(embedded = true)
@Serializable
open class Flags :
    RealmObject(),
    BaseObject,
    AvatarFlags {
    @Serializable(with = TutorialStepListSerializer::class)
    var tutorial: RealmList<TutorialStep>? = null
    var showTour = false
    var dropsEnabled = false
    var itemsEnabled = false
    var newStuff = false
    var lastNewStuffRead: String? = null
    override var classSelected = false
    var rebirthEnabled = false
    var lastFreeRebirth: Date? = null
    var welcomed = false
    var armoireEnabled = false
    var armoireOpened = false
    var armoireEmpty = false
    var communityGuidelinesAccepted = false
    var verifiedUsername = false
    var isWarnedLowHealth = false
}
