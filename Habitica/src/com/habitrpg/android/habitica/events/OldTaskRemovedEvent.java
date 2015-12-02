package com.habitrpg.android.habitica.events;

/**
 * Created by Negue on 01.12.2015.
 */
public class OldTaskRemovedEvent {
    public String deletedTaskId;

    public OldTaskRemovedEvent(String id) {
        deletedTaskId = id;
    }
}
