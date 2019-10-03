package com.habitrpg.shared.habitica.models.members


import com.habitrpg.shared.habitica.models.user.AvatarPreferences
import com.habitrpg.shared.habitica.models.user.Hair


expect open class MemberPreferences : AvatarPreferences {

    override var userId: String?

    override var hair: Hair?
    override var costume: Boolean
    override var isDisableClasses: Boolean
    override var isSleep: Boolean
    override var shirt: String?
    override var skin: String?
    override var size: String?
    override var background: String?
    override var chair: String?
}
