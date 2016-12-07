package com.playseeds.android.sdk;
import com.google.gson.JsonElement;

public interface IUserBehaviorQueryListener {
    void onUserBehaviorResponse(String errorMessage, JsonElement result, String queryPath);
}
