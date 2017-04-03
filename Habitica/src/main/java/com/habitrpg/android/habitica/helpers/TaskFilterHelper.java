package com.habitrpg.android.habitica.helpers;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magicmicky on 02/10/15.
 */
public class TaskFilterHelper {
    private List<String> tagsId;
    private String activeFilter;

    public TaskFilterHelper() {
        tagsId = new ArrayList<>();

    }

    public void addTags(String tags) {
        this.tagsId.add(tags);
    }

    public int howMany() {
        return this.tagsId.size() + (activeFilter != null ? 1 : 0);
    }

    public List<String> getTags() {
        return this.tagsId;
    }

    public void setTags(List<String> tagsId) {
        this.tagsId = tagsId;
    }

    public boolean isTagChecked(String tagID) {
        return this.tagsId.contains(tagID);
    }

    public List<Task> filter(List<Task> tasks) {
        List<Task> filtered = new ArrayList<Task>();
        for (Task task : tasks) {
            if (isFiltered(task)) {
                filtered.add(task);
            }
        }

        return filtered;
    }

    private boolean isFiltered(Task task) {
        if (!task.containsAllTagIds(tagsId)) {
            return false;
        }
        if (activeFilter != null && !activeFilter.equals(Task.FILTER_ALL)) {
            switch (activeFilter) {
                case Task.FILTER_ACTIVE:
                    if (task.type.equals(Task.TYPE_DAILY)) {
                        return task.isDisplayedActive(0);
                    } else {
                        return !task.completed;
                    }
                case Task.FILTER_GRAY:
                    return task.completed || !task.isDisplayedActive(0);
                case Task.FILTER_WEAK:
                    return task.value < 0;
                case Task.FILTER_STRONG:
                    return task.value >= 0;
                case Task.FILTER_DATED:
                    return task.duedate != null;
                case Task.FILTER_COMPLETED:
                    return task.completed;
            }
        }
        return true;
    }

    public void setActiveFilter(String activeFilter) {
        this.activeFilter = activeFilter;
    }
}
