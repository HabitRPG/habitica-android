package com.habitrpg.android.habitica.models.inventory.owned

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OwnedEquipment : RealmObject() {

    @PrimaryKey
    var combinedKey: String? = ""
    var userID: String? = ""
    var key: String? = ""
}