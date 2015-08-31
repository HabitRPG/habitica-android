package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.R;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;


/**
 * Created by Negue on 22.08.2015.
 */
public class ChatMessage {

    static HashMap<Integer, Integer> contributorColorDict;

    static {
        contributorColorDict = new HashMap<>();
        contributorColorDict.put(0, R.color.contributor_0);
        contributorColorDict.put(1, R.color.contributor_1);
        contributorColorDict.put(2, R.color.contributor_2);
        contributorColorDict.put(3, R.color.contributor_3);
        contributorColorDict.put(4, R.color.contributor_4);
        contributorColorDict.put(5, R.color.contributor_5);
        contributorColorDict.put(6, R.color.contributor_6);
        contributorColorDict.put(7, R.color.contributor_7);
        contributorColorDict.put(8, R.color.contributor_mod);
        contributorColorDict.put(9, R.color.contributor_staff);
    }


    public String id;

    public String text;

    public long timestamp;

    // TODO LIKES

    // TODO Flags

    public String uuid;

    public Contributor contributor;

    public Backer backer;

    public String user;

    public int getContributorColor() {
        int rColor = android.R.color.black;


        if (contributor != null) {
                if (contributorColorDict.containsKey(contributor.level)) {
                    rColor = contributorColorDict.get(contributor.level);
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

    public String getAgoString() {
        long diff = new Date().getTime() - timestamp;

        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        if (diffDays != 0) {
            return diffDays + " days ago";
        }

        if (diffHours != 0) {
            return diffHours + " hours ago";
        }

        return diffMinutes + " minutes ago";
    }
}

