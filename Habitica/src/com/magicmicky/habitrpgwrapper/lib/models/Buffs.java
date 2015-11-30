package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;

/**
 * Created by viirus on 06/07/15.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Buffs extends BasicStats {

    @Column
    private boolean snowball;

    @Column
    private boolean streaks;

    public Buffs() {
        this(false, false);
    }

    public Buffs(boolean snowball, boolean streaks) {
        this.snowball = snowball;
        this.streaks = streaks;
    }

    public boolean getSnowball() {
        return snowball;
    }

    public void setSnowball(boolean snowball) {
        this.snowball = snowball;
    }

    public boolean getStreaks() {
        return streaks;
    }

    public void setStreaks(boolean streaks) {
        this.streaks = streaks;
    }
}
