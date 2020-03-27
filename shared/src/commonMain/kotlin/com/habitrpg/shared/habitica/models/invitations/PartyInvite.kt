package com.habitrpg.shared.habitica.models.invitations

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class PartyInvite : NativeRealmObject(), GenericInvitation {

    @PrimaryKeyAnnotation
    override var id: String? = null
    override var name: String? = null
    override var inviter: String? = null
}
