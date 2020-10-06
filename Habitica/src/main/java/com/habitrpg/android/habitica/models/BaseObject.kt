package com.habitrpg.android.habitica.models

import io.realm.RealmModel


interface BaseObject: RealmModel {
    val realmClass: Class<out RealmModel>
    val primaryIdentifier: String?
    val primaryIdentifierName: String
}