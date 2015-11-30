package com.habitrpg.android.habitica.helpers;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magicmicky on 02/10/15.
 */
public class TagsHelper {
    private List<String> tagsId;

    public TagsHelper() {
        tagsId = new ArrayList<String>();

    }

    public void addTags(String tags) {
        this.tagsId.add(tags);
    }

    public int howMany() {
        return this.tagsId.size();
    }

    public List<String> getTags() {
        return this.tagsId;
    }

    public void setTags(List<String> tagsId) {
        this.tagsId = tagsId;
    }

    public List<Task> filter(List<Task> tasks) {
        List<Task> filtered = new ArrayList<Task>();
        for (Task t : tasks) {
            if (t.containsAllTagIds(this.tagsId)) {
                filtered.add(t);
            }
        }

        return filtered;
    }
}
