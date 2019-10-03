package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativeLibraries.NativeList


expect open class QuestProgress {

    var id: String?
    var key: String?
    var hp: Double
    var rage: Double
    var collect: NativeList<QuestProgressCollect>?
    var down: Float
    var up: Float
}
