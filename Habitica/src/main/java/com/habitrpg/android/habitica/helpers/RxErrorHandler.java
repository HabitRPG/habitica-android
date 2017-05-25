package com.habitrpg.android.habitica.helpers;

import android.util.Log;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

import rx.functions.Action1;

public class RxErrorHandler {

    static private RxErrorHandler instance;
    private CrashlyticsProxy crashlyticsProxy;

    public static void init(CrashlyticsProxy crashlyticsProxy) {
        instance = new RxErrorHandler();
        instance.crashlyticsProxy = crashlyticsProxy;
    }

    public static Action1<Throwable> handleEmptyError() {
        //Can't be turned into a lambda, because it then doesn't work for some reason.
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                RxErrorHandler.reportError(throwable);
            }
        };
    }

    private static void reportError(Throwable throwable) {
        if (BuildConfig.DEBUG) {
            Log.e("ObservableError", Log.getStackTraceString(throwable));
        } else {
            instance.crashlyticsProxy.logException(throwable);
        }
    }
}
