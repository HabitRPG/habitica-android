package com.habitrpg.android.habitica.events.commands;

import com.magicmicky.habitrpgwrapper.lib.models.Tag;

/**
 * Created by jjbillings on 8/17/16.
 */
public class UpdateTagCommand {
    public Tag tag;
    public String uuid;

    public  UpdateTagCommand(Tag tag, String uuid) {
        this.tag = tag;
        this.uuid = uuid;
    }
}
