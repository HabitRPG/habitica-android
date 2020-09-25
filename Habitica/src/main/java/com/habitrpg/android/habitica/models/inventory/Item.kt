package com.habitrpg.android.habitica.models.inventory

import io.realm.RealmModel

interface Item : RealmModel {
    val type: String
    val key: String
    val text: String
    val value: Int
}