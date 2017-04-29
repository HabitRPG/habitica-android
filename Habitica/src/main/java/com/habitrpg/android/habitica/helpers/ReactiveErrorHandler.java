package com.habitrpg.android.habitica.helpers;

import android.util.Log;

import rx.android.BuildConfig;
import rx.functions.Action1;

public class ReactiveErrorHandler {

    public static Action1<Throwable> handleEmptyError() {
        //Can't be turned into a lambda, because it then doesn't work for some reason.
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                throwable.printStackTrace();
                if (BuildConfig.DEBUG) {
                    Log.e("ObservableError", Log.getStackTraceString(throwable));
                }
            }
        };
    }
}
