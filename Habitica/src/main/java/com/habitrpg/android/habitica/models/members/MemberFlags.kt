package com.habitrpg.android.habitica.models.members

import com.habitrpg.shared.habitica.models.AvatarFlags
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class MemberFlags :
    RealmObject(),
    AvatarFlags {
    var chatShadowMuted: Boolean = false
    var chatRevoked: Boolean = false
    override var classSelected: Boolean = false
}
