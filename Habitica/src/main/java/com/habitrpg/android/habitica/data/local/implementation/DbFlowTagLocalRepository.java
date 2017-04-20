package com.habitrpg.android.habitica.data.local.implementation;


import android.database.sqlite.SQLiteDoneException;

import com.habitrpg.android.habitica.data.local.TagLocalRepository;
import com.habitrpg.android.habitica.models.Tag;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class DbFlowTagLocalRepository implements TagLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<List<Tag>> getTags(String userId) {
        return Observable.defer(() -> Observable.just(new Select().from(Tag.class)
                .where(Condition.column("user_id").eq(userId))
                .orderBy(OrderBy.columns("position", "dateCreated").descending())
                .queryList()));
    }

    @Override
    public void removeOldTags(List<Tag> onlineTags) {
        final ArrayList<String> onlineTaskTagItemIdList = new ArrayList<>();

        for (Tag item : onlineTags) {
            onlineTaskTagItemIdList.add(item.getId());
        }

        From<Tag> query = new Select().from(Tag.class);
        try {
            if (query.count() != onlineTags.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<Tag>>() {
                    @Override
                    public void onResultReceived(List<Tag> items) {

                        ArrayList<Tag> tagsToDelete = new ArrayList<>();

                        for (Tag tag : items) {
                            if (!onlineTaskTagItemIdList.contains(tag.getId())) {
                                tagsToDelete.add(tag);
                            }
                        }

                        for (Tag tag : tagsToDelete) {
                            tag.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<Tag>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<Tag>> transaction, List<Tag> result) {
                        return result != null && result.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }
    }
}
