package com.habitrpg.android.habitica.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.habitrpg.android.habitica.R;


/**
 * Created by franzejr on 03/11/15.
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(hasInternetConnection(context))
        {
            Toast.makeText(context, R.string.network_up, Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(context, R.string.network_error_no_network_body, Toast.LENGTH_LONG).show();

        }
    }

    boolean hasInternetConnection(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
