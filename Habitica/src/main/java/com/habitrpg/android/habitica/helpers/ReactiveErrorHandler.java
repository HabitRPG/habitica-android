package com.habitrpg.android.habitica.helpers;

import android.util.Log;

import rx.android.BuildConfig;
import rx.functions.Action1;

public class ReactiveErrorHandler {

    public static Action1<Throwable> handleEmptyError() {
        return throwable -> {
            if (BuildConfig.DEBUG) {
                Log.e("ObservableError", Log.getStackTraceString(throwable));
            }
        };
    }
}
