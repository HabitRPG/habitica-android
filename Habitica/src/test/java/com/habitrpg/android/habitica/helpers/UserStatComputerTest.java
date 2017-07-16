package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keith Holliday on 3/31/2017.
 */

public class UserStatComputerTest {

    private UserStatComputer userStatComputer;
    private User user;
    private Equipment equipment;
    private List<Equipment> equipmentList;
    private String key;
    private String text;

    private int str = 1;
    private int intStat = 2;
    private int per = 3;
    private int con = 4;

    public UserStatComputerTest () {
        userStatComputer = new UserStatComputer();
        user = new User();
        Stats stats = new Stats();
        user.setStats(stats);

        key = "example-key";
        text = "example-text";

        equipment = new Equipment();
        equipment.setKey(key);
        equipment.setText(text);
        equipment.setStr(str);
        equipment.set_int(intStat);
        equipment.setPer(per);
        equipment.setCon(con);

        equipmentList = new ArrayList<>();
        equipmentList.add(equipment);
    }

//    @Test
//    public void shouldReturnCorrectEquipmentRow () {
//        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
//        UserStatComputer.EquipmentRow equipmentRow = (UserStatComputer.EquipmentRow) statsRows.get(0);
//
//        Assert.assertEquals(key, equipmentRow.gearKey);
//        Assert.assertEquals(text, equipmentRow.text);
//        Assert.assertEquals("STR 1, INT 2, CON 4, PER 3", equipmentRow.stats);
//    }

//    @Test
//    public void shouldReturnClassBonusRowWhenClassMatches () {
//        user.getStats().setHabitClass(HabitRpgClass.rogue);
//        equipment.setKlass(HabitRpgClass.rogue.toString());
//
//        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
//        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);
//
//        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.labelId);
//        Assert.assertEquals(str * 0.5f, attributeRow.strVal);
//        Assert.assertEquals(intStat * 0.0f, attributeRow.intVal);
//        Assert.assertEquals(con * 0.0f, attributeRow.conVal);
//        Assert.assertEquals(per * 0.5f, attributeRow.perVal);
//        Assert.assertFalse(attributeRow.roundDown);
//        Assert.assertFalse(attributeRow.isSummary);
//    }

//    @Test
//    public void ShouldReturnClassBonusRowWhenSpecialClassMatches () {
//        user.getStats().setHabitClass(HabitRpgClass.rogue);
//        equipment.setKlass("");
//        equipment.setSpecialClass(HabitRpgClass.rogue.toString());
//
//        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
//        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);
//
//        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.labelId);
//        Assert.assertEquals(str * 0.5f, attributeRow.strVal);
//        Assert.assertEquals(intStat * 0.0f, attributeRow.intVal);
//        Assert.assertEquals(con * 0.0f, attributeRow.conVal);
//        Assert.assertEquals(per * 0.5f, attributeRow.perVal);
//        Assert.assertFalse(attributeRow.roundDown);
//        Assert.assertFalse(attributeRow.isSummary);
//    }

//    @Test
//    public void shouldNotReturnClassBonusWhenClassDoesNotMatch () {
//        user.getStats().setHabitClass(HabitRpgClass.rogue);
//        equipment.setKlass("");
//        equipment.setSpecialClass("");
//
//        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
//        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);
//
//        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.labelId);
//        Assert.assertEquals(str *0.0f, attributeRow.strVal);
//        Assert.assertEquals(intStat * 0.0f, attributeRow.intVal);
//        Assert.assertEquals(con * 0.0f, attributeRow.conVal);
//        Assert.assertEquals(per * 0.0f, attributeRow.perVal);
//        Assert.assertFalse(attributeRow.roundDown);
//        Assert.assertFalse(attributeRow.isSummary);
//    }
}
