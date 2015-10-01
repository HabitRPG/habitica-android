package com.habitrpg.android.habitica;

import android.app.Application;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;

import com.instabug.library.Instabug;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.io.File;

/**
 * Created by Negue on 14.06.2015.
 */
public class HabiticaApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FlowManager.init(this);

        Instabug.DEBUG = BuildConfig.DEBUG;
        Instabug.initialize(this, "a5aa5f471a9cd8a958c0c55181172655");
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name,
                                               int mode, SQLiteDatabase.CursorFactory factory) {
        return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name,
                                               int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return super.openOrCreateDatabase(getDatabasePath(name).getAbsolutePath(), mode, factory, errorHandler);
    }

    @Override
    public boolean deleteDatabase(String name) {
        return super.deleteDatabase(getDatabasePath(name).getAbsolutePath());
    }

    @Override
    public File getDatabasePath(String name) {
        return new File(getExternalFilesDir(null), "HabiticaDatabase/"+name);
    }
}
