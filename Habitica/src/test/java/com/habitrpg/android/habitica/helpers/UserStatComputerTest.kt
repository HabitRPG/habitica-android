package com.habitrpg.android.habitica.helpers

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.UserStatComputer.AttributeRow
import com.habitrpg.android.habitica.helpers.UserStatComputer.EquipmentRow
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Stats
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class UserStatComputerTest : StringSpec({
    val userStatComputer = UserStatComputer()
    val user = Member()
    user.stats = Stats()
    val equipment = Equipment()
    val equipmentList: MutableList<Equipment> = ArrayList()
    val key = "example-key"
    val text = "example-text"
    val str = 1
    val intStat = 2
    val per = 3
    val con = 4

    equipment.key = key
    equipment.text = text
    equipment.str = str
    equipment.intelligence = intStat
    equipment.per = per
    equipment.con = con
    equipmentList.add(equipment)

    "should return correct equipment row" {
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val equipmentRow = statsRows[0] as EquipmentRow
        key shouldBe equipmentRow.gearKey
        text shouldBe equipmentRow.text
        "STR 1, INT 2, CON 4, PER 3" shouldBe equipmentRow.stats
    }

    "should return class bonus for rogue" {
        user.stats!!.habitClass = Stats.ROGUE
        equipment.klass = Stats.ROGUE
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        R.string.profile_class_bonus.toLong() shouldBe attributeRow.labelId.toLong()
        (str * 0.5f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.0f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.0f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.5f).toDouble() shouldBe attributeRow.perVal.toDouble()
        attributeRow.roundDown shouldBe false
        attributeRow.summary shouldBe false
    }

    "should return class bonus for healer" {
        user.stats!!.habitClass = Stats.HEALER
        equipment.klass = Stats.HEALER
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        (str * 0.0f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.5f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.5f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.0f).toDouble() shouldBe attributeRow.perVal.toDouble()
    }

    "should return class bonus for warrior" {
        user.stats!!.habitClass = Stats.WARRIOR
        equipment.klass = Stats.WARRIOR
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        (str * 0.5f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.0f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.5f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.0f).toDouble() shouldBe attributeRow.perVal.toDouble()
    }

    "should return class bonus for mage" {
        user.stats!!.habitClass = Stats.MAGE
        equipment.klass = Stats.MAGE
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        (str * 0.0f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.5f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.0f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.5f).toDouble() shouldBe attributeRow.perVal.toDouble()
    }

    "should return class bonus when special class matches" {
        user.stats!!.habitClass = Stats.ROGUE
        equipment.klass = ""
        equipment.specialClass = Stats.ROGUE
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        R.string.profile_class_bonus.toLong() shouldBe attributeRow.labelId.toLong()
        (str * 0.5f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.0f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.0f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.5f).toDouble() shouldBe attributeRow.perVal.toDouble()
        attributeRow.roundDown shouldBe false
        attributeRow.summary shouldBe false
    }

    "should not return class bonus when it does not match" {
        user.stats?.habitClass = Stats.ROGUE
        equipment.klass = ""
        equipment.specialClass = ""
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        R.string.profile_class_bonus.toLong() shouldBe attributeRow.labelId.toLong()
        (str * 0.0f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.0f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.0f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.0f).toDouble() shouldBe attributeRow.perVal.toDouble()
        attributeRow.roundDown shouldBe false
        attributeRow.summary shouldBe false
    }
})
