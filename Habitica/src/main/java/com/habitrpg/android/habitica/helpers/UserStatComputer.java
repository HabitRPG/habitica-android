package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.tasks.ItemData;

import java.util.ArrayList;
import java.util.List;

public class UserStatComputer {

    // @TODO: Not really sure if this is correct
    public interface StatsRow {

    }

    public class AttributeRow implements StatsRow {
        public int labelId;
        public float strVal;
        public float intVal;
        public float conVal;
        public float perVal;
        public boolean roundDown;
        public boolean isSummary;
    }

    public class EquipmentRow implements StatsRow {
        public String gearKey;
        public String text;
        public String stats;
    }

    public List<StatsRow> computeClassBonus (List<ItemData> itemDataList, HabitRPGUser user) {
        List<StatsRow> skillRows = new ArrayList<>();

        float strAttributes = 0;
        float intAttributes = 0;
        float conAttributes = 0;
        float perAttributes = 0;

        float strClassBonus = 0;
        float intClassBonus = 0;
        float conClassBonus = 0;
        float perClassBonus = 0;

        // Summarize stats and fill equipment table
        for (ItemData i : itemDataList) {
            int str_ = (int) i.getStr();
            int int_ = (int) i.get_int();
            int con_ = (int) i.getCon();
            int per_ = (int) i.getPer();

            strAttributes += str_;
            intAttributes += int_;
            conAttributes += con_;
            perAttributes += per_;

            StringBuilder sb = new StringBuilder();

            if (str_ != 0) {
                sb.append("STR ").append(str_).append(", ");
            }
            if (int_ != 0) {
                sb.append("INT ").append(int_).append(", ");
            }
            if (con_ != 0) {
                sb.append("CON ").append(con_).append(", ");
            }
            if (per_ != 0) {
                sb.append("PER ").append(per_).append(", ");
            }

            // remove the last comma
            if (sb.length() > 2) {
                sb.delete(sb.length() - 2, sb.length());
            }

            EquipmentRow equipmentRow = new EquipmentRow();
            equipmentRow.gearKey = i.getKey();
            equipmentRow.text = i.getText();
            equipmentRow.stats = sb.toString();
            skillRows.add(equipmentRow);

            // Calculate class bonus
            String itemClass = i.getKlass();
            String itemSpecialClass = i.getSpecialClass();

            Boolean classDoesNotExist = itemClass == null || itemClass.isEmpty();
            Boolean specialClassDoesNotExist = itemSpecialClass == null || itemSpecialClass.isEmpty();

            if (classDoesNotExist && specialClassDoesNotExist) {
                continue;
            }

            float classBonus = 0.5f;
            Boolean userClassMatchesGearClass = !classDoesNotExist && itemClass.equals(user.getStats().get_class().toString());
            Boolean userClassMatchesGearSpecialClass = !specialClassDoesNotExist && itemSpecialClass.equals(user.getStats().get_class().toString());

            if (!userClassMatchesGearClass && !userClassMatchesGearSpecialClass) classBonus = 0;

            if (itemClass == null || itemClass.isEmpty()) {
                itemClass = itemSpecialClass;
            }

            if (itemClass != null) {
                switch (itemClass) {
                    case "rogue":
                        strClassBonus = str_ * classBonus;
                        perClassBonus = per_ * classBonus;
                        break;
                    case "healer":
                        conClassBonus = con_ * classBonus;
                        intClassBonus = int_ * classBonus;
                        break;
                    case "warrior":
                        strClassBonus = str_ * classBonus;
                        conClassBonus = con_ * classBonus;
                        break;
                    case "wizard":
                        intClassBonus = int_ * classBonus;
                        perClassBonus = per_ * classBonus;
                        break;
                }
            }
        }

        AttributeRow attributeRow = new AttributeRow();
        attributeRow.labelId = R.string.battle_gear;
        attributeRow.strVal = strAttributes;
        attributeRow.intVal = intAttributes;
        attributeRow.conVal = conAttributes;
        attributeRow.perVal = perAttributes;
        attributeRow.roundDown = true;
        attributeRow.isSummary = false;
        skillRows.add(attributeRow);


        AttributeRow attributeRow2 = new AttributeRow();
        attributeRow2.labelId = R.string.profile_class_bonus;
        attributeRow2.strVal = strClassBonus;
        attributeRow2.intVal = intClassBonus;
        attributeRow2.conVal = conClassBonus;
        attributeRow2.perVal = perClassBonus;
        attributeRow2.roundDown = false;
        attributeRow2.isSummary = false;
        skillRows.add(attributeRow2);

        return skillRows;
    }
}
