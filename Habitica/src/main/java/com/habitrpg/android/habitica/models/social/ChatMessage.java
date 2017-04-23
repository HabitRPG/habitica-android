package com.habitrpg.android.habitica.models.social;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.user.ContributorInfo;

import android.content.res.Resources;

import java.util.Date;
import java.util.HashMap;


/**
 * Created by Negue on 22.08.2015.
 */
public class ChatMessage {

    public String id;

    public String text;

    public CharSequence parsedText;

    public Long timestamp;

    public HashMap<String, Boolean> likes;

    public int flagCount;

    public String uuid;

    public Contributor contributor;

    public Backer backer;

    public String user;

    public String sent;

    public int getContributorColor() {
        int rColor = android.R.color.black;


        if (contributor != null) {
            if (ContributorInfo.CONTRIBUTOR_COLOR_DICT.containsKey(contributor.level)) {
                rColor = ContributorInfo.CONTRIBUTOR_COLOR_DICT.get(contributor.level);
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

