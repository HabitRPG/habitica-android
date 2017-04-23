package com.habitrpg.android.habitica.models.user;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.models.auth.LocalAuthentication;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by admin on 18/11/15.
 */
@Table(databaseName = HabitDatabase.NAME)
public class Authentication extends BaseModel {

    @SerializedName("local")
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "localauthentication_user_id",
            columnType = String.class,
            foreignColumnName = "user_id")})
    public LocalAuthentication localAuthentication;
    @Column
    @PrimaryKey
    @NotNull
    String user_id;

    public LocalAuthentication getLocalAuthentication() {
        return localAuthentication;
    }

    public void setLocalAuthentication(LocalAuthentication LocalAuthentication) {
        this.localAuthentication = LocalAuthentication;
    }

    @Override
    public void save() {

        if (localAuthentication != null)
            localAuthentication.user_id = user_id;

        super.save();
    }
}
