package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Description of a Tag in HabitRPG
 * Created by MagicMicky on 16/03/14.
 */
public class Tag {
    String id;
    String name;

    public Tag() {
        this(null,null);
    }

    public Tag(String id, String name) {
        this.setId(id);
        this.setName(name);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
