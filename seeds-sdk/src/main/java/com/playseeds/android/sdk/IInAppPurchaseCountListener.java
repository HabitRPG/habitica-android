package com.playseeds.android.sdk;

public interface IInAppPurchaseCountListener {
    void onInAppPurchaseCount(String errorMessage, int purchasesCount, String key);
}
