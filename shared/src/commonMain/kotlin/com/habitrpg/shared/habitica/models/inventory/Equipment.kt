package com.habitrpg.shared.habitica.models.inventory

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation


open class Equipment : NativeRealmObject() {

    var value: Double = 0.toDouble()
    var type: String? = ""
    @PrimaryKeyAnnotation
    var key: String? = ""
    var klass: String = ""
    var specialClass: String = ""
    var index: String = ""
    var text: String = ""
    var notes: String = ""
    var con: Int = 0
    var str: Int = 0
    var per: Int = 0
    @SerializedNameAnnotation("int")
    var _int: Int = 0
    var owned: Boolean? = null
    var twoHanded = false
    var mystery = ""
    var gearSet = ""
}
