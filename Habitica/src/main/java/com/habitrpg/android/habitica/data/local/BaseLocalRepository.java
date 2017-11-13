package com.habitrpg.android.habitica.data.local;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

public interface BaseLocalRepository {

    void close();

    void executeTransaction(Realm.Transaction transaction);

    <T extends RealmObject> T getUnmanagedCopy(T object);
    <T extends RealmObject> List<T> getUnmanagedCopy(List<T> list);

    <T extends RealmObject>void save(List<T> objects);
    <T extends RealmObject>void save(T object);

    boolean isClosed();
}
