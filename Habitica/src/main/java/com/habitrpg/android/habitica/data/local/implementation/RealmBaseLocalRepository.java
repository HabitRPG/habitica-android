package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.BaseLocalRepository;

import io.realm.Realm;

abstract class RealmBaseLocalRepository implements BaseLocalRepository {

    Realm realm;

    RealmBaseLocalRepository(Realm realm) {
        this.realm = realm;
    }

    @Override
    public void close() {
        realm.close();
    }

    @Override
    public void executeTransaction(Realm.Transaction transaction) {
        realm.executeTransaction(transaction);
    }
}
