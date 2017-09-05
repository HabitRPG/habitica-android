package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.social.ChatMessage;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmObject;

public interface BaseLocalRepository {

    void close();

    void executeTransaction(Realm.Transaction transaction);

    <T extends RealmObject> T getUnmanagedCopy(T object);
    <T extends RealmObject> List<T> getUnmanagedCopy(List<T> list);

    <T extends RealmObject>void save(List<T> objects);
    <T extends RealmObject>void save(T object);
}
