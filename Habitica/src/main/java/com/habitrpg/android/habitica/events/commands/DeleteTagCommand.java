package com.habitrpg.android.habitica.events.commands;

import com.habitrpg.android.habitica.models.Tag;

/**
 * Created by jjbillings on 8.9.2016.
 */
public class DeleteTagCommand {
    public Tag tag;

    public DeleteTagCommand(Tag tag) {
        this.tag = tag;
    }
}

