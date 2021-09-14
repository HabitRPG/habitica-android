package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject

interface OwnedObject : BaseObject {
    var userID: String?
    var key: String?
}
