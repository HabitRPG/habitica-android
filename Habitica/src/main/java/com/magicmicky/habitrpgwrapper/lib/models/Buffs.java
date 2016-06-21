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
    private Boolean snowball;

    @Column
    private Boolean streaks;

    public Buffs() {
        this(false, false);
    }

    public Buffs(Boolean snowball, Boolean streaks) {
        this.snowball = snowball;
        this.streaks = streaks;
    }

    public Boolean getSnowball() {
        return snowball != null ? snowball : Boolean.FALSE;
    }

    public void setSnowball(Boolean snowball) {
        this.snowball = snowball;
    }

    public Boolean getStreaks() {
        return streaks != null ? streaks : Boolean.FALSE;
    }

    public void setStreaks(Boolean streaks) {
        this.streaks = streaks;
    }

    public void merge(Buffs stats) {
        if (stats == null) {
            return;
        }
        super.merge(stats);
        this.snowball = stats.snowball != null ? stats.snowball : this.snowball;
        this.streaks = stats.streaks != null ? stats.streaks : this.streaks;
    }
}
