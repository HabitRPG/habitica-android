package com.magicmicky.habitrpgwrapper.lib.models;

import android.content.res.Resources;

import com.habitrpg.android.habitica.R;

import java.util.Date;
import java.util.HashMap;


/**
 * Created by Negue on 22.08.2015.
 */
public class ChatMessage {

    private static final HashMap<Integer, Integer> CONTRIBUTOR_COLOR_DICT;

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


    public String id;

    public String text;

    public CharSequence parsedText;

    public long timestamp;

    public HashMap<String, Boolean> likes;

    public int flagCount;

    public String uuid;

    public Contributor contributor;

    public Backer backer;

    public String user;

    public int getContributorColor() {
        int rColor = android.R.color.black;


        if (contributor != null) {
                if (CONTRIBUTOR_COLOR_DICT.containsKey(contributor.level)) {
                    rColor = CONTRIBUTOR_COLOR_DICT.get(contributor.level);
                }
        }

        if (backer != null) {
            if (backer.npc != null) {
                rColor = android.R.color.black;
            }
        }

        return rColor;
    }

    public int getContributorForegroundColor() {
        int rColor = android.R.color.white;

        if (backer != null && backer.npc != null) {
            rColor = R.color.contributor_npc_font;
        }

        return rColor;
    }

    public String getAgoString(Resources res) {
        long diff = new Date().getTime() - timestamp;

        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        if (diffDays != 0) {
            if (diffDays == 1) {
                return res.getString(R.string.ago_1day);
            }
            return res.getString(R.string.ago_days, diffDays);
        }

        if (diffHours != 0) {
            if (diffHours == 1) {
                return res.getString(R.string.ago_1hour);
            }
            return res.getString(R.string.ago_hours, diffHours);
        }

        if (diffMinutes == 1) {
            return res.getString(R.string.ago_1Minute);
        }
        return res.getString(R.string.ago_minutes, diffMinutes);
    }
}

