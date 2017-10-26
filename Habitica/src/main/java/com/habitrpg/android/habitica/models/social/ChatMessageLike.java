package com.habitrpg.android.habitica.models.social;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by phillip on 30.06.17.
 */

public class ChatMessageLike extends RealmObject {

    @PrimaryKey
    public String id;

    public ChatMessageLike(String id) {
        super();
        this.id = id;
    }

    public ChatMessageLike() {
        super();
    }
}
