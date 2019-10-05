package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativeLibraries.NativeRealmList


expect open class QuestProgress {
    var id: String?
    var key: String?
    var hp: Double
    var rage: Double
    var collect: NativeRealmList<QuestProgressCollect>?
    var down: Float
    var up: Float
}
