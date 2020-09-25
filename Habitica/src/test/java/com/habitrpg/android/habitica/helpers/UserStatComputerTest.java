package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.inventory.Equipment;
import com.habitrpg.android.habitica.models.members.Member;
import com.habitrpg.android.habitica.models.user.Stats;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class UserStatComputerTest {

    private UserStatComputer userStatComputer;
    private Member user;
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
        user = new Member();
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

    @Test
    public void shouldReturnCorrectEquipmentRow () {
        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
        UserStatComputer.EquipmentRow equipmentRow = (UserStatComputer.EquipmentRow) statsRows.get(0);

        Assert.assertEquals(key, equipmentRow.getGearKey());
        Assert.assertEquals(text, equipmentRow.getText());
        Assert.assertEquals("STR 1, INT 2, CON 4, PER 3", equipmentRow.getStats());
    }

    @Test
    public void shouldReturnClassBonusRowWhenClassMatches () {
        user.getStats().setHabitClass(Stats.ROGUE);
        equipment.setKlass(Stats.ROGUE);

        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);

        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.getLabelId());
        Assert.assertEquals(str * 0.5f, attributeRow.getStrVal());
        Assert.assertEquals(intStat * 0.0f, attributeRow.getIntVal());
        Assert.assertEquals(con * 0.0f, attributeRow.getConVal());
        Assert.assertEquals(per * 0.5f, attributeRow.getPerVal());
        Assert.assertFalse(attributeRow.getRoundDown());
        Assert.assertFalse(attributeRow.getSummary());
    }

    @Test
    public void ShouldReturnClassBonusRowWhenSpecialClassMatches () {
        user.getStats().setHabitClass(Stats.ROGUE);
        equipment.setKlass("");
        equipment.setSpecialClass(Stats.ROGUE);

        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);

        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.getLabelId());
        Assert.assertEquals(str * 0.5f, attributeRow.getStrVal());
        Assert.assertEquals(intStat * 0.0f, attributeRow.getIntVal());
        Assert.assertEquals(con * 0.0f, attributeRow.getConVal());
        Assert.assertEquals(per * 0.5f, attributeRow.getPerVal());
        Assert.assertFalse(attributeRow.getRoundDown());
        Assert.assertFalse(attributeRow.getSummary());
    }

    @Test
    public void shouldNotReturnClassBonusWhenClassDoesNotMatch () {
        user.getStats().setHabitClass(Stats.ROGUE);
        equipment.setKlass("");
        equipment.setSpecialClass("");

        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(equipmentList, user);
        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);

        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.getLabelId());
        Assert.assertEquals(str *0.0f, attributeRow.getStrVal());
        Assert.assertEquals(intStat * 0.0f, attributeRow.getIntVal());
        Assert.assertEquals(con * 0.0f, attributeRow.getConVal());
        Assert.assertEquals(per * 0.0f, attributeRow.getPerVal());
        Assert.assertFalse(attributeRow.getRoundDown());
        Assert.assertFalse(attributeRow.getSummary());
    }
}
