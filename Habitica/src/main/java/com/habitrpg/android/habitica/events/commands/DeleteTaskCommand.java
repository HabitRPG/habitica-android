package com.habitrpg.android.habitica.events.commands;

/**
 * Created by Negue on 02.12.2015.
 */
public class DeleteTaskCommand {
    public String TaskIdToDelete;

    public DeleteTaskCommand(String id) {
        TaskIdToDelete = id;
    }
}
