package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.ContentResult;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.user.User;

import io.realm.Realm;
import io.realm.RealmObject;

public interface BaseLocalRepository {

    void close();

    void executeTransaction(Realm.Transaction transaction);

    <T extends RealmObject> T getUnmanagedCopy(T object);
}
