package com.habitrpg.android.habitica.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class FAQArticle : RealmObject() {

    @PrimaryKey
    var position: Int? = null

    var question: String? = null
    var answer: String? = null
}
