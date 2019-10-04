package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativeLibraries.RealmListWrapper


expect open class QuestProgress {
    var id: String?
    var key: String?
    var hp: Double
    var rage: Double
    var collect: RealmListWrapper<QuestProgressCollect>?
    var down: Float
    var up: Float
}
