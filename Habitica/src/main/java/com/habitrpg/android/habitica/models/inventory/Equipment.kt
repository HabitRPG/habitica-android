package com.habitrpg.android.habitica.models.inventory

import com.habitrpg.android.habitica.models.BaseMainObject
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class Equipment :
    RealmObject(),
    BaseMainObject {
    var value: Double = 0.toDouble()
    var type: String? = ""

    @PrimaryKey
    var key: String? = ""
    var klass: String = ""
    var specialClass: String = ""
    var index: String = ""
    var text: String = ""
    var notes: String = ""
    var con: Int = 0
    var str: Int = 0
    var per: Int = 0
    var released: Boolean = true

    @SerialName("int")
    var intelligence: Int = 0
    var twoHanded = false
    var mystery = ""
    var gearSet = ""

    override val realmClass: Class<out RealmModel>
        get() = Equipment::class.java
    override val primaryIdentifier: String?
        get() = key
    override val primaryIdentifierName: String
        get() = "key"
}
