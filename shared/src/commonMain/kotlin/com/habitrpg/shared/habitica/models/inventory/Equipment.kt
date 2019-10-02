package com.habitrpg.shared.habitica.models.inventory


expect open class Equipment {
    var value: Double
    var type: String?
    var key: String?
    var klass: String
    var specialClass: String
    var index: String
    var text: String
    var notes: String
    var con: Int
    var str: Int
    var per: Int
    var _int: Int
    var owned: Boolean?
    var twoHanded: Boolean?
}
