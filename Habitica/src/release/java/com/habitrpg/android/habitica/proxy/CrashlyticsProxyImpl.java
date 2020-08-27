package com.habitrpg.android.habitica.proxy;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.crashlytics.internal.common.CrashlyticsCore;
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

import android.content.Context;

import org.jetbrains.annotations.NotNull;


public class CrashlyticsProxyImpl implements CrashlyticsProxy {

    @Override
    public void logException(@NotNull Throwable t) {
        FirebaseCrashlytics.getInstance().recordException(t);
    }

    @Override
    public void setUserIdentifier(@NotNull String identifier) {
        FirebaseCrashlytics.getInstance().setUserId(identifier);
    }

    @Override
    public void log(@NotNull String msg) {
        FirebaseCrashlytics.getInstance().log(msg);
    }
}
