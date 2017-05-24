package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.models.inventory.Mount;
import com.habitrpg.android.habitica.models.inventory.Pet;
import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.realm.RealmList;

import static org.junit.Assert.assertEquals;

@Config(constants = BuildConfig.class)
@RunWith(RobolectricTestRunner.class)
public class UserTest {

    private User user;

    @Before
    public void setup() {
        user = new User();
        Items items = new Items();
        user.setItems(items);
    }

    @Test
    public void getPetsFoundCount_shouldReturnSumOfAllPetEntries() {
        RealmList<Pet> pets = new RealmList<>();
        pets.add(new Pet());
        pets.add(new Pet());
        pets.add(new Pet());
        pets.add(new Pet());
        pets.add(new Pet());

        user.getItems().setPets(pets);
        assertEquals(5, user.getPetsFoundCount());
    }

    @Test
    public void getPetsFoundCount_onNoPetCollectionAvailable_shouldReturnZero() {
        assertEquals(0, user.getPetsFoundCount());
    }

    @Test
    public void getMountsTamedCount_shouldReturnSumOfAllMountEntries() {
        RealmList<Mount> mounts = new RealmList<>();
        mounts.add(new Mount());
        mounts.add(new Mount());
        mounts.add(new Mount());
        mounts.add(new Mount());
        mounts.add(new Mount());

        user.getItems().setMounts(mounts);
        assertEquals(5, user.getMountsTamedCount());
    }

    @Test
    public void getMountsTamedCount_onNoMountCollectionAvailable_shouldReturnZero() {
        assertEquals(0, user.getMountsTamedCount());
    }
}