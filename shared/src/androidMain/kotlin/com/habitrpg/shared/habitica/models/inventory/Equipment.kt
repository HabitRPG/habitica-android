package com.habitrpg.shared.habitica.models.inventory

import com.google.gson.annotations.SerializedName

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Equipment : RealmObject() {

    actual var value: Double = 0.toDouble()
    actual var type: String? = ""
    @PrimaryKey
    actual var key: String? = ""
    actual var klass: String = ""
    actual var specialClass: String = ""
    actual var index: String = ""
    actual var text: String = ""
    actual var notes: String = ""
    actual var con: Int = 0
    actual var str: Int = 0
    actual var per: Int = 0
    @SerializedName("int")
    actual var _int: Int = 0
    actual var owned: Boolean? = null
    actual var twoHanded: Boolean? = false
}
