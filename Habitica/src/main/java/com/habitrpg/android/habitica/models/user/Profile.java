package com.habitrpg.android.habitica.models.user;

/**
 * Created by MagicMicky on 16/03/14.
 */

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(databaseName = HabitDatabase.NAME)
public class Profile extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    String user_Id;

    @Column
    private String name;

    @Column
    private String blurb, imageUrl;

    public Profile(String name) {
        this(name, "", "");
    }

    public Profile(String name, String blurb, String imageUrl) {
        this.name = name;
        this.blurb = blurb;
        this.imageUrl = imageUrl;
    }

    public Profile() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String blurb) {
        this.blurb = blurb;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
