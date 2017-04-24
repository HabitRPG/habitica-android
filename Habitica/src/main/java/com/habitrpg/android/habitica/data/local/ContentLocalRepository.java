package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.ContentResult;

public interface ContentLocalRepository extends BaseLocalRepository {
    void saveContent(ContentResult contentResult);
}
