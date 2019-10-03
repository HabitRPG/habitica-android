package com.habitrpg.shared.habitica.models.social

import com.habitrpg.shared.habitica.models.inventory.Quest


expect open class UserParty  {
    var userId: String?
    var id: String
    var quest: Quest?
    var partyOrder: String? //Order to display ppl
    var orderAscending: String? //Order type
}
