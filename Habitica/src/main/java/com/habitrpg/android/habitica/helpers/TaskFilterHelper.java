package com.habitrpg.android.habitica.helpers;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by magicmicky on 02/10/15.
 */
public class TaskFilterHelper {
    private List<String> tagsId;
    private Map<String, String> activeFilters = new HashMap<>();

    public TaskFilterHelper() {
        tagsId = new ArrayList<>();

    }

    public void addTags(String tags) {
        this.tagsId.add(tags);
    }

    public int howMany(String type) {
        return this.tagsId.size() + (activeFilters.get(type) != null || Task.FILTER_ACTIVE.equals(activeFilters.get(type)) ? 1 : 0);
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
        List<Task> filtered = new ArrayList<>();
        String activeFilter = activeFilters.get(tasks.get(0).type);
        for (Task task : tasks) {
            if (isFiltered(task, activeFilter)) {
                filtered.add(task);
            }
        }

        return filtered;
    }

    private boolean isFiltered(Task task, String activeFilter) {
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

    public void setActiveFilter(String type, String activeFilter) {
        activeFilters.put(type, activeFilter);
    }

    public String getActiveFilter(String type) {
        return activeFilters.get(type);
    }
}
