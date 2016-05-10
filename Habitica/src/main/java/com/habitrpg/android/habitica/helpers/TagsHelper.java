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

    public void setTags(List<String> tagsId) {
        this.tagsId = tagsId;
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

    public boolean isTagChecked(String tagID) {
        return this.tagsId.contains(tagID);
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

    public List<Task> filterDue(List<Task> tasks, int offset) {
        if (tasks.size() > 0 && !tasks.get(0).getType().equals(Task.TYPE_DAILY)) return tasks;
        List<Task> filtered = new ArrayList<Task>();
        for (Task t : tasks) {
            if (t.getType().equals(Task.TYPE_DAILY))
            if (t.isDisplayedActive(offset))
                filtered.add(t);
        }
        return filtered;
    }
}
