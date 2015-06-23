package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magicmicky on 15/05/15.
 */
public class Tags {
    private List<String> tags;

    public Tags() {
        this.tags = new ArrayList<>();
    }
    public Tags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
