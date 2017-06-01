package com.habitrpg.android.habitica.events.commands;

/**
 * Created by Negue on 02.12.2015.
 */
public class DeleteTaskCommand {
    public String TaskIdToDelete;
    public boolean ignoreEvent;

    public DeleteTaskCommand(String id) {
        this(id, false);
    }

    public DeleteTaskCommand(String id, boolean ignore) {
        TaskIdToDelete = id;
        ignoreEvent = ignore;
    }
}
