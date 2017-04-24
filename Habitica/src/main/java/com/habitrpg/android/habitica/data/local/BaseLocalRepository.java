package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.ContentResult;

import io.realm.Realm;

public interface BaseLocalRepository {

    void close();

    void executeTransaction(Realm.Transaction transaction);
}
