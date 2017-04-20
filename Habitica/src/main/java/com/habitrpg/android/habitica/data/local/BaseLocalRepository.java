package com.habitrpg.android.habitica.data.local;

import io.realm.Realm;

public interface BaseLocalRepository {

    void close();

    void executeTransaction(Realm.Transaction transaction);
}
