package com.habitrpg.android.habitica.helpers

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.UserStatComputer.AttributeRow
import com.habitrpg.android.habitica.helpers.UserStatComputer.EquipmentRow
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Stats
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.util.ArrayList

class UserStatComputerTest {
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
        assertEquals(key, equipmentRow.gearKey)
        assertEquals(text, equipmentRow.text)
        assertEquals("STR 1, INT 2, CON 4, PER 3", equipmentRow.stats)
    }

    @Test
    fun shouldReturnClassBonusRowWhenClassMatches() {
        user.stats!!.habitClass = Stats.ROGUE
        equipment.klass = Stats.ROGUE
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        assertEquals(R.string.profile_class_bonus.toLong(), attributeRow.labelId.toLong())
        assertEquals((str * 0.5f).toDouble(), attributeRow.strVal.toDouble(), 0.01)
        assertEquals((intStat * 0.0f).toDouble(), attributeRow.intVal.toDouble(), 0.01)
        assertEquals((con * 0.0f).toDouble(), attributeRow.conVal.toDouble(), 0.01)
        assertEquals((per * 0.5f).toDouble(), attributeRow.perVal.toDouble(), 0.01)
        assertFalse(attributeRow.roundDown)
        assertFalse(attributeRow.summary)
    }

    @Test
    fun ShouldReturnClassBonusRowWhenSpecialClassMatches() {
        user.stats!!.habitClass = Stats.ROGUE
        equipment.klass = ""
        equipment.specialClass = Stats.ROGUE
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        assertEquals(R.string.profile_class_bonus.toLong(), attributeRow.labelId.toLong())
        assertEquals((str * 0.5f).toDouble(), attributeRow.strVal.toDouble(), 0.01)
        assertEquals((intStat * 0.0f).toDouble(), attributeRow.intVal.toDouble(), 0.01)
        assertEquals((con * 0.0f).toDouble(), attributeRow.conVal.toDouble(), 0.01)
        assertEquals((per * 0.5f).toDouble(), attributeRow.perVal.toDouble(), 0.01)
        assertFalse(attributeRow.roundDown)
        assertFalse(attributeRow.summary)
    }

    @Test
    fun shouldNotReturnClassBonusWhenClassDoesNotMatch() {
        user.stats?.habitClass = Stats.ROGUE
        equipment.klass = ""
        equipment.specialClass = ""
        val statsRows = userStatComputer.computeClassBonus(equipmentList, user)
        val attributeRow = statsRows[2] as AttributeRow
        assertEquals(R.string.profile_class_bonus.toLong(), attributeRow.labelId.toLong())
        assertEquals((str * 0.0f).toDouble(), attributeRow.strVal.toDouble(), 0.01)
        assertEquals((intStat * 0.0f).toDouble(), attributeRow.intVal.toDouble(), 0.01)
        assertEquals((con * 0.0f).toDouble(), attributeRow.conVal.toDouble(), 0.01)
        assertEquals((per * 0.0f).toDouble(), attributeRow.perVal.toDouble())
        assertFalse(attributeRow.roundDown)
        assertFalse(attributeRow.summary)
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
