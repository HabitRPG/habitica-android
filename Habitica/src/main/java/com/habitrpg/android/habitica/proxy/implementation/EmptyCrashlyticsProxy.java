package com.habitrpg.android.habitica.proxy.implementation;


import android.content.Context;

import com.habitrpg.android.habitica.proxy.CrashlyticsProxy;

public class EmptyCrashlyticsProxy implements CrashlyticsProxy {
    @Override
    public void init(Context context) {
        //pass
    }

    @Override
    public void logException(Throwable e) {
        //pass
    }

    @Override
    public void setString(String key, String value) {
        //pass
    }

    @Override
    public void setUserIdentifier(String identifier) {
        //pass
    }

    @Override
    public void setUserName(String name) {

    }

    @Override
    public void fabricLogE(String s1, String s2, Exception e) {
        //pass
    }

    @Override
    public void log(String msg) {
        //pass
    }
}
