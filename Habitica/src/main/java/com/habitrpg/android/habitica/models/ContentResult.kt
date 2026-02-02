package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.inventory.EquipmentSet
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.social.CategoryOption
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
    var mystery = RealmList<EquipmentSet>()
    var categoryOptions = RealmList<CategoryOption>()
}
