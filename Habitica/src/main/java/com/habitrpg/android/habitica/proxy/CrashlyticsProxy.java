package com.habitrpg.android.habitica.proxy;


import android.content.Context;

public interface CrashlyticsProxy {
    void init(Context context);

    void logException(Throwable t);

    void setString(String key, String value);

    void setUserIdentifier(String identifier);

    void setUserName(String name);

    void fabricLogE(String s1, String s2, Exception e);
}
