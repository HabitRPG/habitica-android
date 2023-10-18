package com.habitrpg.android.habitica.models.inventory

interface Animal {
    var key: String
    var text: String?
    var type: String?
    var animal: String
    var color: String
    var premium: Boolean
    var numberOwned: Int
    var totalNumber: Int
}
