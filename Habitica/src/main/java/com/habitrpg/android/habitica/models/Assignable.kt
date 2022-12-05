package com.habitrpg.android.habitica.models

import com.habitrpg.shared.habitica.models.Avatar

interface Assignable {
    val id: String?
    val avatar: Avatar?
    val identifiableName: String
}
