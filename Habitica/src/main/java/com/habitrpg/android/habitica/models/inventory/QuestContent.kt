package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestContent : RealmObject(), Item {

    @PrimaryKey
    internal var key: String = ""
    set(value) {
        field = value
        drop?.key = value
        colors?.key = value
        boss?.key = value
    }
    internal var text: String = ""
    var notes: String = ""
    internal var value: Int = 0
    var previous: String? = null
    var lvl: Int = 0
    var isCanBuy: Boolean = false
    var category: String? = null
    var boss: QuestBoss? = null
        set(boss) {
            field = boss
            if (boss != null) {
                boss.key = key
            }
        }
    var drop: QuestDrops? = null
        set(drop) {
            field = drop
            if (drop != null) {
                drop.key = key
            }
        }
    var colors: QuestColors? = null
        set(colors) {
            field = colors
            if (colors != null) {
                colors.key = key
            }
        }

    var collect: RealmList<QuestCollect>? = null

    val isBossQuest: Boolean
        get() = this.boss != null

    override fun getType(): String {
        return "quests"
    }

    override fun getKey(): String {
        return key
    }

    override fun getText(): String {
        return text
    }

    override fun getValue(): Int? {
        return value
    }

    fun setText(text: String) {
        this.text = text
    }

    fun setValue(value: Int) {
        this.value = value
    }

    fun setKey(key: String) {
        this.key = key
    }

    fun getCollectWithKey(key: String): QuestCollect? {
        for (collect in this.collect ?: emptyList<QuestCollect>()) {
            if (collect.key == key) {
                return collect
            }
        }
        return null
    }

    fun hasGifImage(): Boolean {
        val gifImageKeys = listOf("lostMasterclasser4")
        if (gifImageKeys.contains(key)) {
            return true
        }
        return false
    }
}
