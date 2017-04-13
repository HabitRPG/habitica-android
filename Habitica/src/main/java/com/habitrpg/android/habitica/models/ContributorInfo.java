package com.habitrpg.android.habitica.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.R;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.HashMap;

/**
 * Created by keithholliday on 7/6/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class ContributorInfo extends BaseModel {

    public static final HashMap<Integer, Integer> CONTRIBUTOR_COLOR_DICT;

    static {
        CONTRIBUTOR_COLOR_DICT = new HashMap<>();
        CONTRIBUTOR_COLOR_DICT.put(0, R.color.contributor_0);
        CONTRIBUTOR_COLOR_DICT.put(1, R.color.contributor_1);
        CONTRIBUTOR_COLOR_DICT.put(2, R.color.contributor_2);
        CONTRIBUTOR_COLOR_DICT.put(3, R.color.contributor_3);
        CONTRIBUTOR_COLOR_DICT.put(4, R.color.contributor_4);
        CONTRIBUTOR_COLOR_DICT.put(5, R.color.contributor_5);
        CONTRIBUTOR_COLOR_DICT.put(6, R.color.contributor_6);
        CONTRIBUTOR_COLOR_DICT.put(7, R.color.contributor_7);
        CONTRIBUTOR_COLOR_DICT.put(8, R.color.contributor_mod);
        CONTRIBUTOR_COLOR_DICT.put(9, R.color.contributor_staff);
    }

    @Column
    @PrimaryKey
    @NotNull
    public String user_id;

    @SerializedName("admin")
    @Expose
    private boolean admin;

    @SerializedName("contributions")
    @Expose
    private String contributions;

    @SerializedName("level")
    @Expose
    private int level;

    @SerializedName("text")
    @Expose
    private String text;

    public Boolean getAdmin() {
        return this.admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getContributions() {
        return this.contributions;
    }

    public void setContributions(String contributions) {
        this.contributions = contributions;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getContributorColor() {
        int rColor = android.R.color.black;


        if (CONTRIBUTOR_COLOR_DICT.containsKey(this.level)) {
            rColor = CONTRIBUTOR_COLOR_DICT.get(this.level);
        }

        return rColor;
    }

    public int getContributorForegroundColor() {
        int rColor = android.R.color.white;
        return rColor;
    }
}
