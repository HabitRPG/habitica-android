package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import com.google.gson.annotations.SerializedName;
import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Negue on 13.07.2015.
 */
@Table(databaseName = HabitDatabase.NAME)
public class ItemData extends BaseModel {
    public ItemData(){}

    @Column
    public double value;

    @Column
    public String type;

    @PrimaryKey
    @Column
    public String key;

    @Column
    public String klass;

    @Column(name = "_index")
    public String index;

    @Column
    public String text;

    @Column
    public String notes;

    @Column
    public float con, str, per;

    @Column
    @SerializedName("int")
    public float _int;

    @Column
    public Boolean owned;
}
