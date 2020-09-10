package com.habitrpg.android.habitica.models.user;

import android.util.SparseIntArray;

import com.habitrpg.android.habitica.R;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ContributorInfo extends RealmObject {

    public static final SparseIntArray CONTRIBUTOR_COLOR_DICT;

    static {
        CONTRIBUTOR_COLOR_DICT = new SparseIntArray();
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

    @PrimaryKey
    private String userId;

    public User user;
    private boolean admin;
    private String contributions;
    private int level;
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
        int rColor = R.color.text_primary;

        if (CONTRIBUTOR_COLOR_DICT.get(this.level, -1) > 0) {
            rColor = CONTRIBUTOR_COLOR_DICT.get(this.level);
        }

        return rColor;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
