package com.habitrpg.android.habitica.helpers

import com.habitrpg.android.habitica.BaseAnnotationTestCase
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.UserStatComputer.AttributeRow
import com.habitrpg.android.habitica.helpers.UserStatComputer.EquipmentRow
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Stats
import io.kotest.matchers.shouldBe
import java.util.ArrayList

class UserStatComputerTest : BaseAnnotationTestCase() {
    private val userStatComputer: UserStatComputer = UserStatComputer()
    private val user: Member = Member()
    private val equipment: Equipment
    private val equipmentList: MutableList<Equipment>
    private val key: String
    private val text: String
    private val str = 1
    private val intStat = 2
    private val per = 3
    private val con = 4
    @Test
    fun shouldReturnCorrectEquipmentRow() {
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val equipmentRow = statsRows[0] as EquipmentRow
        key shouldBe equipmentRow.gearKey
        text shouldBe equipmentRow.text
        "STR 1, INT 2, CON 4, PER 3" shouldBe equipmentRow.stats
    }

    @Test
    fun shouldReturnClassBonusRowWhenClassMatches() {
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

    @Test
    fun shouldReturnClassBonusForHealer() {
        user.stats!!.habitClass = Stats.HEALER
        equipment.klass = Stats.HEALER
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        (str * 0.0f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.5f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.5f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.0f).toDouble() shouldBe attributeRow.perVal.toDouble()
    }

    @Test
    fun shouldReturnClassBonusForWarrior() {
        user.stats!!.habitClass = Stats.WARRIOR
        equipment.klass = Stats.WARRIOR
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        (str * 0.5f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.0f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.5f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.0f).toDouble() shouldBe attributeRow.perVal.toDouble()
    }

    @Test
    fun shouldReturnClassBonusForMage() {
        user.stats!!.habitClass = Stats.MAGE
        equipment.klass = Stats.MAGE
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        (str * 0.0f).toDouble() shouldBe attributeRow.strVal.toDouble()
        (intStat * 0.5f).toDouble() shouldBe attributeRow.intVal.toDouble()
        (con * 0.0f).toDouble() shouldBe attributeRow.conVal.toDouble()
        (per * 0.5f).toDouble() shouldBe attributeRow.perVal.toDouble()
    }

    @Test
    fun ShouldReturnClassBonusRowWhenSpecialClassMatches() {
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

    @Test
    fun shouldNotReturnClassBonusWhenClassDoesNotMatch() {
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

    init {
        val stats = Stats()
        user.stats = stats
        key = "example-key"
        text = "example-text"
        equipment = Equipment()
        equipment.key = key
        equipment.text = text
        equipment.str = str
        equipment._int = intStat
        equipment.per = per
        equipment.con = con
        equipmentList = ArrayList()
        equipmentList.add(equipment)
    }
}
