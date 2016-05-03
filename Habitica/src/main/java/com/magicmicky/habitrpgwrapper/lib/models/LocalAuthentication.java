package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by admin on 18/11/15.
 */
@Table(databaseName = HabitDatabase.NAME)
public class LocalAuthentication extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    String user_id;

    @Column
    String email, username;

    public String getEmail() { return email; }
    public String getUsername() { return username; }

    public void setEmail(String email) {this.email = email; }
    public void setUsername(String username) {this.username = username; }
}
