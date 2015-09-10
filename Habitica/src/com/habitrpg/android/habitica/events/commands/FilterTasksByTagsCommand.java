package com.habitrpg.android.habitica.events.commands;

import java.util.ArrayList;

/**
 * Created by Negue on 09.09.2015.
 */
public class FilterTasksByTagsCommand {
    public ArrayList<String> tagList;

    public FilterTasksByTagsCommand(ArrayList<String> tagList) {
        this.tagList = tagList;
    }
}
