package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.nativeLibraries.NativeDate

expect open class TutorialStep  {
    var key: String?
    var tutorialGroup: String?
    var identifier: String?
    var wasCompleted: Boolean
    var displayedOn: NativeDate?
    fun shouldDisplay(): Boolean
}
