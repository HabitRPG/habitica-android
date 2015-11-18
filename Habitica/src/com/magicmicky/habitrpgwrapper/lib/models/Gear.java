package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by viirus on 20/07/15.
 */

@Table(databaseName = HabitDatabase.NAME)
public class Gear extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    String user_id;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "equipped_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    private Outfit equipped;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "costume_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    private Outfit costume;

    public Outfit getCostume() {
        return costume;
    }

    public void setCostume(Outfit costume) {
        this.costume = costume;
    }

    public Outfit getEquipped() {
        return equipped;
    }

    public void setEquipped(Outfit equipped) {
        this.equipped = equipped;
    }

    @Override
    public void save() {
        equipped.user_id = user_id+"_equipped";
        costume.user_id = user_id+"_costume";

        super.save();
    }
}
