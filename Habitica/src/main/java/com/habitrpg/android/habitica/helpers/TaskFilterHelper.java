package com.habitrpg.android.habitica.helpers;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.OrderedRealmCollection;
import io.realm.RealmQuery;

public class TaskFilterHelper {
    private List<String> tagsId;
    private Map<String, String> activeFilters = new HashMap<>();

    public TaskFilterHelper() {
        tagsId = new ArrayList<>();

    }

    public void addTags(String tags) {
        this.tagsId.add(tags);
    }

    public int howMany(@Nullable String type) {
        return this.tagsId.size() + (isTaskFilterActive(type) ? 1 : 0);
    }

    private boolean isTaskFilterActive(@Nullable String type) {
        if (activeFilters.get(type) == null) {
            return false;
        }
        if (Task.TYPE_TODO.equals(type)) {
            return !Task.FILTER_ACTIVE.equals(activeFilters.get(type));
        } else {
            return !Task.FILTER_ALL.equals(activeFilters.get(type));
        }
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
        if (tasks.size() == 0) {
            return tasks;
        }
        List<Task> filtered = new ArrayList<>();
        String activeFilter = null;
        if (activeFilters != null && activeFilters.size() > 0) {
            activeFilter = activeFilters.get(tasks.get(0).type);
        }
        for (Task task : tasks) {
            if (isFiltered(task, activeFilter)) {
                filtered.add(task);
            }
        }

        return filtered;
    }

    private boolean isFiltered(Task task, @Nullable String activeFilter) {
        if (!task.containsAllTagIds(tagsId)) {
            return false;
        }
        if (activeFilter != null && !activeFilter.equals(Task.FILTER_ALL)) {
            switch (activeFilter) {
                case Task.FILTER_ACTIVE:
                    if (task.type.equals(Task.TYPE_DAILY)) {
                        return task.isDisplayedActive();
                    } else {
                        return !task.completed;
                    }
                case Task.FILTER_GRAY:
                    return task.completed || !task.isDisplayedActive();
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

    public RealmQuery<Task> createQuery(OrderedRealmCollection<Task> unfilteredData) {
        RealmQuery<Task> query = unfilteredData.where();

        if (unfilteredData.size() == 0) {
            return query;
        }

        String taskType = unfilteredData.get(0).getType();
        String activeFilter = getActiveFilter(taskType);

        if (tagsId != null && tagsId.size() > 0) {
            query = query.in("tags.id", tagsId.toArray(new String[0]));
        }
        if (activeFilter != null && !activeFilter.equals(Task.FILTER_ALL)) {
            switch (activeFilter) {
                case Task.FILTER_ACTIVE:
                    if (Task.TYPE_DAILY.equals(taskType)) {
                        query = query.equalTo("completed", false).equalTo("isDue", true);
                    } else {
                        query = query.equalTo("completed", false);
                    }
                    break;
                case Task.FILTER_GRAY:
                    query = query.equalTo("completed", true).or().equalTo("isDue", false);
                break;
                case Task.FILTER_WEAK:
                    query = query.lessThan("value", 0.0d);
                    break;
                case Task.FILTER_STRONG:
                    query = query.greaterThanOrEqualTo("value", 0.0d);
                    break;
                case Task.FILTER_DATED:
                    query = query.isNotNull("duedate");
                    break;
                case Task.FILTER_COMPLETED:
                    query = query.equalTo("completed", true);
                    break;
            }
        }
        return query;
    }
}
