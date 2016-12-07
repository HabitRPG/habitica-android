package com.playseeds.android.sdk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.vending.billing.IInAppBillingService;
import com.playseeds.android.sdk.inappmessaging.InAppMessageListener;

/**
 * Created by atte on 23/08/16.
 *
 * Streamlines the integration experience on Android v4.0 and up by
 * - automating the resolving of the billing service
 * - listening to onStart, onStop and onDestroy of the activity and informing Seeds SDK
 *   about the changes in application state
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivityEventListener implements Application.ActivityLifecycleCallbacks {
    private Activity mainActivity;
    private final InAppMessageListener listener;
    private final String serverURL;
    private final String appKey;
    private final String deviceID;
    private final DeviceId.Type idMode;
    private ServiceConnection mServiceConn;
    IInAppBillingService mService;

    public MainActivityEventListener(Activity mainActivity, InAppMessageListener listener, String serverURL, String appKey, String deviceID, DeviceId.Type idMode) {
        this.mainActivity = mainActivity;
        this.listener = listener;
        this.serverURL = serverURL;
        this.appKey = appKey;
        this.deviceID = deviceID;
        this.idMode = idMode;
    }

    public void resolve() {
        mainActivity.getApplication().registerActivityLifecycleCallbacks(this);

        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);

                Seeds.sharedInstance()
                        .init(mainActivity, mService, listener, serverURL, appKey, deviceID, idMode);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");

        mainActivity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity == mainActivity) {
            Log.d(Seeds.TAG, "mainactivity onstart");
            Seeds.sharedInstance().onStart();
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity == mainActivity) {
            Log.d(Seeds.TAG, "mainactivity onstop");
            Seeds.sharedInstance().onStop();
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity == mainActivity) {
            if (mService != null) {
                Log.d(Seeds.TAG, "mainactivity onactivitydestroyed");
                mainActivity.unbindService(mServiceConn);
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // Unneeded
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // Unneeded
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // Unneeded
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // Unneeded
    }
}