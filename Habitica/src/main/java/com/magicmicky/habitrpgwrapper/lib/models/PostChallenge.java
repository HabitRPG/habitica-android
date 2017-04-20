package com.magicmicky.habitrpgwrapper.lib.models;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

/**
 * Needs to be a separate Class, the normal Challenge has the group as Object
 * The Challenge for adding/updating just has the group as id
 */
public class PostChallenge {
    public String id;

    public String name;

    public String shortName;

    public String description;

    public String leaderName;

    public String group;

    public int prize;

    public TasksOrder tasksOrder;
}
