package com.habitrpg.android.habitica;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = HabitDatabase.NAME, version = HabitDatabase.VERSION, foreignKeysSupported = true)
public class HabitDatabase {

    public static final String NAME = "Habitica";

    public static final int VERSION = 36;

    public HabitDatabase() {
        super();
    }
}
