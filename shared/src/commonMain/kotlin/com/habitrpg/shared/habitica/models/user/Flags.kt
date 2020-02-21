package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.models.TutorialStep
import com.habitrpg.shared.habitica.nativePackages.NativeList
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class Flags : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var tutorial: NativeList<TutorialStep> = NativeList()
    var showTour: Boolean = false
    var dropsEnabled: Boolean = false
    var itemsEnabled: Boolean = false
    var newStuff: Boolean = false
    var classSelected: Boolean = false
    var rebirthEnabled: Boolean = false
    var welcomed: Boolean = false
    var armoireEnabled: Boolean = false
    var armoireOpened: Boolean = false
    var armoireEmpty: Boolean = false
    var isCommunityGuidelinesAccepted: Boolean = false
    var isVerifiedUsername: Boolean = false
    var isWarnedLowHealth: Boolean = false

    fun getTutorial(): List<TutorialStep> {
        return tutorial
    }

    fun setTutorial(tutorial: NativeList<TutorialStep>) {
        this.tutorial = tutorial
    }
}
