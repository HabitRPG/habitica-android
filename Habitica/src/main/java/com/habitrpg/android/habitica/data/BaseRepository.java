package com.habitrpg.android.habitica.data;

import java.util.List;

import io.realm.RealmObject;

public interface BaseRepository {

    void close();

    <T extends RealmObject> T getUnmanagedCopy(T object);
    <T extends RealmObject> List<T> getUnmanagedCopy(List<T> list);

    boolean isClosed();
}
