package com.habitrpg.android.habitica;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by viirus on 06/07/15.
 */
@Database(name = HabitDatabase.NAME, version = HabitDatabase.VERSION, foreignKeysSupported = true)
public class HabitDatabase {

    public static final String NAME = "Habitica";

    public static final int VERSION = 2;
}
