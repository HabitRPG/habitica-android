package com.habitrpg.android.habitica.proxy;


import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;

import android.content.Context;

import io.fabric.sdk.android.Fabric;

public class CrashlyticsProxyImpl implements CrashlyticsProxy {
    @Override
    public void init(Context context) {
        Crashlytics crashlytics = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().build())
                .build();
        Fabric.with(context, crashlytics);
    }


    @Override
    public void logException(Throwable t) {
        Crashlytics.logException(t);
    }

    @Override
    public void setString(String key, String value) {
        Crashlytics.setString(key, value);
    }

    @Override
    public void setUserIdentifier(String identifier) {
        if (Crashlytics.getInstance().core != null && identifier != null) {
            Crashlytics.getInstance().core.setUserIdentifier(identifier);
        }
    }

    @Override
    public void setUserName(String name) {
        if (Crashlytics.getInstance().core != null && name != null) {
            Crashlytics.getInstance().core.setUserName(name);
        }
    }

    @Override
    public void fabricLogE(String s1, String s2, Exception e) {
        Fabric.getLogger().e(s1,s2,e);
    }
}
