package com.habitrpg.android.habitica.helpers

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.shared.habitica.models.Avatar

class UserStatComputer {

    interface StatsRow

    inner class AttributeRow : StatsRow {
        var labelId: Int = 0
        var strVal: Float = 0.toFloat()
        var intVal: Float = 0.toFloat()
        var conVal: Float = 0.toFloat()
        var perVal: Float = 0.toFloat()
        var roundDown: Boolean = false
        var summary: Boolean = false
    }

    inner class EquipmentRow : StatsRow {
        var gearKey: String? = null
        var text: String? = null
        var stats: String? = null
    }

    fun computeClassBonus(equipmentList: List<Equipment>?, user: Avatar): List<StatsRow> {
        val skillRows = ArrayList<StatsRow>()

        var strAttributes = 0f
        var intAttributes = 0f
        var conAttributes = 0f
        var perAttributes = 0f

        var strClassBonus = 0f
        var intClassBonus = 0f
        var conClassBonus = 0f
        var perClassBonus = 0f

        // Summarize stats and fill equipment table
        for (i in equipmentList ?: emptyList()) {
            val strength = i.str
            val intelligence = i._int
            val constitution = i.con
            val perception = i.per

            strAttributes += strength.toFloat()
            intAttributes += intelligence.toFloat()
            conAttributes += constitution.toFloat()
            perAttributes += perception.toFloat()

            val sb = StringBuilder()

            if (strength != 0) {
                sb.append("STR ").append(strength).append(", ")
            }
            if (intelligence != 0) {
                sb.append("INT ").append(intelligence).append(", ")
            }
            if (constitution != 0) {
                sb.append("CON ").append(constitution).append(", ")
            }
            if (perception != 0) {
                sb.append("PER ").append(perception).append(", ")
            }

            // remove the last comma
            if (sb.length > 2) {
                sb.delete(sb.length - 2, sb.length)
            }

            val equipmentRow = EquipmentRow()
            equipmentRow.gearKey = i.key
            equipmentRow.text = i.text
            equipmentRow.stats = sb.toString()
            skillRows.add(equipmentRow)

            // Calculate class bonus
            var itemClass: String? = i.klass
            val itemSpecialClass = i.specialClass

            val classDoesNotExist = itemClass == null || itemClass.isEmpty()
            val specialClassDoesNotExist = itemSpecialClass.isEmpty()

            if (classDoesNotExist && specialClassDoesNotExist) {
                continue
            }

            var classBonus = 0.5f
            val userClassMatchesGearClass = !classDoesNotExist && itemClass == user.stats?.habitClass
            val userClassMatchesGearSpecialClass = !specialClassDoesNotExist && itemSpecialClass == user.stats?.habitClass

            if (!userClassMatchesGearClass && !userClassMatchesGearSpecialClass) classBonus = 0f

            if (itemClass == null || itemClass.isEmpty() || itemClass == "special") {
                itemClass = itemSpecialClass
            }

            when (itemClass) {
                Stats.ROGUE -> {
                    strClassBonus += strength * classBonus
                    perClassBonus += perception * classBonus
                }
                Stats.HEALER -> {
                    conClassBonus += constitution * classBonus
                    intClassBonus += intelligence * classBonus
                }
                Stats.WARRIOR -> {
                    strClassBonus += strength * classBonus
                    conClassBonus += constitution * classBonus
                }
                Stats.MAGE -> {
                    intClassBonus += intelligence * classBonus
                    perClassBonus += perception * classBonus
                }
            }
        }

        val attributeRow = AttributeRow()
        attributeRow.labelId = R.string.battle_gear
        attributeRow.strVal = strAttributes
        attributeRow.intVal = intAttributes
        attributeRow.conVal = conAttributes
        attributeRow.perVal = perAttributes
        attributeRow.roundDown = true
        attributeRow.summary = false
        skillRows.add(attributeRow)

        val attributeRow2 = AttributeRow()
        attributeRow2.labelId = R.string.profile_class_bonus
        attributeRow2.strVal = strClassBonus
        attributeRow2.intVal = intClassBonus
        attributeRow2.conVal = conClassBonus
        attributeRow2.perVal = perClassBonus
        attributeRow2.roundDown = false
        attributeRow2.summary = false
        skillRows.add(attributeRow2)

        return skillRows
    }
}
