package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.models.user.Items;
import com.habitrpg.android.habitica.models.user.OwnedMount;
import com.habitrpg.android.habitica.models.user.OwnedPet;
import com.habitrpg.android.habitica.models.user.User;

import org.junit.Before;
import org.junit.Test;

import io.realm.RealmList;

import static org.junit.Assert.assertEquals;

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
        RealmList<OwnedPet> pets = new RealmList<>();
        pets.add(new OwnedPet());
        pets.add(new OwnedPet());
        pets.add(new OwnedPet());
        pets.add(new OwnedPet());
        pets.add(new OwnedPet());

        user.getItems().setPets(pets);
        assertEquals(5, user.getPetsFoundCount());
    }

    @Test
    public void getPetsFoundCount_onNoPetCollectionAvailable_shouldReturnZero() {
        assertEquals(0, user.getPetsFoundCount());
    }

    @Test
    public void getMountsTamedCount_shouldReturnSumOfAllMountEntries() {
        RealmList<OwnedMount> mounts = new RealmList<>();
        mounts.add(new OwnedMount());
        mounts.add(new OwnedMount());
        mounts.add(new OwnedMount());
        mounts.add(new OwnedMount());
        mounts.add(new OwnedMount());

        user.getItems().setMounts(mounts);
        assertEquals(5, user.getMountsTamedCount());
    }

    @Test
    public void getMountsTamedCount_onNoMountCollectionAvailable_shouldReturnZero() {
        assertEquals(0, user.getMountsTamedCount());
    }
}