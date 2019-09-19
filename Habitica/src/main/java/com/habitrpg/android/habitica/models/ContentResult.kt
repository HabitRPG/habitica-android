package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.inventory.*
import io.realm.RealmList

/**
 * Created by Negue on 15.07.2015.
 */
class ContentResult {

    var potion: Equipment? = null

    var armoire: Equipment? = null

    var gear: ContentGear? = null

    var quests = RealmList<QuestContent>()
    var eggs = RealmList<Egg>()
    var food = RealmList<Food>()
    var hatchingPotions = RealmList<HatchingPotion>()

    var pets = RealmList<Pet>()

    var mounts = RealmList<Mount>()

    var spells = RealmList<Skill>()

    var appearances = RealmList<Customization>()
    var backgrounds = RealmList<Customization>()

    var faq = RealmList<FAQArticle>()
    var special = RealmList<SpecialItem>()
}

