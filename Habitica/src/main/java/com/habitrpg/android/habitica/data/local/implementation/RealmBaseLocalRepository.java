package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.BaseLocalRepository;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmObject;

public abstract class RealmBaseLocalRepository implements BaseLocalRepository {

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

    @Override
    public <T extends RealmObject> T getUnmanagedCopy(T object) {
        if (object.isManaged() && object.isValid()) {
            return realm.copyFromRealm(object);
        } else {
            return object;
        }
    }

    @Override
    public <T extends RealmObject> List<T> getUnmanagedCopy(List<T> list) {
        return realm.copyFromRealm(list);
    }

    @Override
    public <T extends RealmObject> void save(T object) {
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(object));
    }

    @Override
    public boolean isClosed() {
        return realm.isClosed();
    }

    @Override
    public <T extends RealmObject> void save(List<T> objects) {
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(objects));
    }
}
