package com.habitrpg.android.habitica.helpers;

import android.util.Log;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

import java.io.EOFException;
import java.io.IOException;

import io.reactivex.functions.Consumer;
import okhttp3.internal.http2.ConnectionShutdownException;
import retrofit2.HttpException;

public class RxErrorHandler {

    static private RxErrorHandler instance;
    private CrashlyticsProxy crashlyticsProxy;

    public static void init(CrashlyticsProxy crashlyticsProxy) {
        instance = new RxErrorHandler();
        instance.crashlyticsProxy = crashlyticsProxy;

        //RxJavaHooks.setOnError(handleEmptyError());
    }

    public static Consumer<Throwable> handleEmptyError() {
        //Can't be turned into a lambda, because it then doesn't work for some reason.
        return RxErrorHandler::reportError;
    }

    public static void reportError(Throwable throwable) {
        if (BuildConfig.DEBUG) {
            try {
                Log.e("ObservableError", Log.getStackTraceString(throwable));
            } catch (Exception ignored) {}
        } else {
            if (!IOException.class.isAssignableFrom(throwable.getClass())
                    && !HttpException.class.isAssignableFrom(throwable.getClass())
                    && !retrofit2.HttpException.class.isAssignableFrom(throwable.getClass())
                    && !EOFException.class.isAssignableFrom(throwable.getClass())
            && !ConnectionShutdownException.class.isAssignableFrom(throwable.getClass())) {
                instance.crashlyticsProxy.logException(throwable);
            }
        }
    }
}
