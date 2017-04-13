package com.habitrpg.android.habitica.helpers;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.HabitRPGUser;
import com.habitrpg.android.habitica.models.HabitRpgClass;
import com.habitrpg.android.habitica.models.Stats;
import com.habitrpg.android.habitica.models.tasks.ItemData;

import junit.framework.Assert;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Keith Holliday on 3/31/2017.
 */

public class UserStatComputerTest {

    private UserStatComputer userStatComputer;
    private HabitRPGUser user;
    private ItemData itemData;
    private List<ItemData> itemDataList;
    private String key;
    private String text;

    private int str = 1;
    private int intStat = 2;
    private int per = 3;
    private int con = 4;

    public UserStatComputerTest () {
        userStatComputer = new UserStatComputer();
        user = new HabitRPGUser();
        Stats stats = new Stats();
        user.setStats(stats);

        key = "example-key";
        text = "example-text";

        itemData = new ItemData();
        itemData.setKey(key);
        itemData.setText(text);
        itemData.setStr(str);
        itemData.set_int(intStat);
        itemData.setPer(per);
        itemData.setCon(con);

        itemDataList = new ArrayList<>();
        itemDataList.add(itemData);
    }

    @Test
    public void shouldReturnCorrectEquipmentRow () {
        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(itemDataList, user);
        UserStatComputer.EquipmentRow equipmentRow = (UserStatComputer.EquipmentRow) statsRows.get(0);

        Assert.assertEquals(key, equipmentRow.gearKey);
        Assert.assertEquals(text, equipmentRow.text);
        Assert.assertEquals("STR 1, INT 2, CON 4, PER 3", equipmentRow.stats);
    }

    @Test
    public void shouldReturnClassBonusRowWhenClassMatches () {
        user.getStats().set_class(HabitRpgClass.rogue);
        itemData.setKlass(HabitRpgClass.rogue.toString());

        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(itemDataList, user);
        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);

        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.labelId);
        Assert.assertEquals(str * 0.5f, attributeRow.strVal);
        Assert.assertEquals(intStat * 0.0f, attributeRow.intVal);
        Assert.assertEquals(con * 0.0f, attributeRow.conVal);
        Assert.assertEquals(per * 0.5f, attributeRow.perVal);
        Assert.assertFalse(attributeRow.roundDown);
        Assert.assertFalse(attributeRow.isSummary);
    }

    @Test
    public void ShouldReturnClassBonusRowWhenSpecialClassMatches () {
        user.getStats().set_class(HabitRpgClass.rogue);
        itemData.setKlass("");
        itemData.setSpecialClass(HabitRpgClass.rogue.toString());

        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(itemDataList, user);
        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);

        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.labelId);
        Assert.assertEquals(str * 0.5f, attributeRow.strVal);
        Assert.assertEquals(intStat * 0.0f, attributeRow.intVal);
        Assert.assertEquals(con * 0.0f, attributeRow.conVal);
        Assert.assertEquals(per * 0.5f, attributeRow.perVal);
        Assert.assertFalse(attributeRow.roundDown);
        Assert.assertFalse(attributeRow.isSummary);
    }

    @Test
    public void shouldNotReturnClassBonusWhenClassDoesNotMatch () {
        user.getStats().set_class(HabitRpgClass.rogue);
        itemData.setKlass("");
        itemData.setSpecialClass("");

        List<UserStatComputer.StatsRow> statsRows = userStatComputer.computeClassBonus(itemDataList, user);
        UserStatComputer.AttributeRow attributeRow = (UserStatComputer.AttributeRow) statsRows.get(2);

        Assert.assertEquals(R.string.profile_class_bonus, attributeRow.labelId);
        Assert.assertEquals(str *0.0f, attributeRow.strVal);
        Assert.assertEquals(intStat * 0.0f, attributeRow.intVal);
        Assert.assertEquals(con * 0.0f, attributeRow.conVal);
        Assert.assertEquals(per * 0.0f, attributeRow.perVal);
        Assert.assertFalse(attributeRow.roundDown);
        Assert.assertFalse(attributeRow.isSummary);
    }
}
