package com.habitrpg.shared.habitica.models.user

expect open class ABTest {
    @PrimaryKey
    var userID: String?

    var name: String
    var group: String
}