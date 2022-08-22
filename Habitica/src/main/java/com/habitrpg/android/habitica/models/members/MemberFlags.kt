package com.habitrpg.android.habitica.models.members

import com.habitrpg.shared.habitica.models.AvatarFlags
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class MemberFlags : RealmObject(), AvatarFlags {
    override var classSelected: Boolean = false
}
