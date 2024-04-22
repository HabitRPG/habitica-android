package com.habitrpg.android.habitica.models.invitations

import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class PartyInvite : RealmObject(), GenericInvitation {
    override var id: String? = null
    override var name: String? = null
    override var inviter: String? = null
}
