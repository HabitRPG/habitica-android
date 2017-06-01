package com.habitrpg.android.habitica.models.tasks;

import com.google.gson.Gson;
import com.raizlabs.android.dbflow.converter.TypeConverter;

/**
 * Created by krh12 on 6/1/2017.
 */

@com.raizlabs.android.dbflow.annotation.TypeConverter
public class TaskIntegerListConverter extends TypeConverter<String, TaskIntegerList> {
    @Override
    public String getDBValue(TaskIntegerList model) {
        return new Gson().toJson(model);
    }

    @Override
    public TaskIntegerList getModelValue(String data) {
        return new Gson().fromJson(data, TaskIntegerList.class);
    }
}
