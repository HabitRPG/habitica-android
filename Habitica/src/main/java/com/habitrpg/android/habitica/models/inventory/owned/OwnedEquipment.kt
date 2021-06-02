package com.habitrpg.android.habitica.models.inventory.owned

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class OwnedEquipment : RealmObject(), BaseObject {

    @PrimaryKey
    var combinedKey: String? = ""
    var userID: String? = ""
    var key: String? = ""
}