package com.habitrpg.shared.habitica.models

expect open class Achievement  {
    var key: String?
    var type: String?
    var title: String?
    var text: String?
    var icon: String?
    var category: String?
    var earned: Boolean
    var index: Int
    var optionalCount: Int?
}
