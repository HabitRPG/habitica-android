package com.habitrpg.shared.habitica.models.inventory


expect open class QuestRageStrike() {
    var key: String
    var wasHit: Boolean


    constructor(key: String, wasHit: Boolean)
}

