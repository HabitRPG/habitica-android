package com.habitrpg.shared.habitica.models.invitations


expect class GuildInvite {

    internal var invitations: Invitations?
    /**
     * @return The inviter
     */
    /**
     * @param inviter The inviter
     */
    var inviter: String?
    /**
     * @return The name
     */
    /**
     * @param name The name
     */
    var name: String?
    /**
     * @return The id
     */
    /**
     * @param id The id
     */
    var id: String?

    var publicGuild: Boolean?
}
