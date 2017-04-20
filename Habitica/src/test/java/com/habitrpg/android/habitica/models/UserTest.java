package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.models.user.HabitRPGUser;
import com.habitrpg.android.habitica.models.user.Items;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class UserTest {

    private HabitRPGUser user;

    @Before
    public void setup() {
        user = new HabitRPGUser();
        Items items = new Items();
        user.setItems(items);
    }

    @Test
    public void getPetsFoundCount_shouldReturnSumOfAllPetEntries() {
        HashMap<String, Integer> pets = new HashMap<>();
        pets.put("BearCub-Base", 0);
        pets.put("Sheep-Base", 5);
        pets.put("Cheetah-Shade", 35);
        pets.put("Slime-Base", -1);
        pets.put("Axolotl-Red", 5);

        user.getItems().setPets(pets);
        assertEquals(5, user.getPetsFoundCount());
    }

    @Test
    public void getPetsFoundCount_onNoPetCollectionAvailable_shouldReturnZero() {
        assertEquals(0, user.getPetsFoundCount());
    }

    @Test
    public void getMountsTamedCount_shouldReturnSumOfAllMountEntries() {
        HashMap<String, Boolean> mounts = new HashMap<>();
        mounts.put("BearCub-White", null);
        mounts.put("BearCub-CottonCandyPink", true);
        mounts.put("Seahorse-Base", true);
        mounts.put("Owl-Zombie", true);
        mounts.put("Cactus-White", null);

        user.getItems().setMounts(mounts);
        assertEquals(5, user.getMountsTamedCount());
    }

    @Test
    public void getMountsTamedCount_onNoMountCollectionAvailable_shouldReturnZero() {
        assertEquals(0, user.getMountsTamedCount());
    }
}