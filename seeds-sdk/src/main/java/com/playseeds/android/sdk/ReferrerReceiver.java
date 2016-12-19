package com.playseeds.android.sdk;

import java.net.URLDecoder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * ADB Testing
 * adb shell
 * am broadcast -a com.android.vending.INSTALL_REFERRER --es "referrer" "countly_cid%3Dcb14e5f33b528334715f1809e4572842c74686df%26countly_cuid%3Decf125107e4e27e6bcaacb3ae10ddba66459e6ae"
**/
//******************************************************************************
public class ReferrerReceiver extends BroadcastReceiver
{
    private static String key = "referrer";
    //--------------------------------------------------------------------------
    public static String getReferrer(Context context)
    {
        // Return any persisted referrer value or null if we don't have a referrer.
        return context.getSharedPreferences(key, Context.MODE_PRIVATE).getString(key, null);
    }

    public static void deleteReferrer(Context context)
    {
        // delete stored referrer.
        context.getSharedPreferences(key, Context.MODE_PRIVATE).edit().remove(key).commit();
    }

    //--------------------------------------------------------------------------
    public ReferrerReceiver(){
    }

    //--------------------------------------------------------------------------
    @Override public void onReceive(Context context, Intent intent)
    {
        try
        {
            // Make sure this is the intent we expect - it always should be.
            if ((null != intent) && (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")))
            {
                // This intent should have a referrer string attached to it.
                String rawReferrer = intent.getStringExtra(key);
                if (null != rawReferrer)
                {
                    // The string is usually URL Encoded, so we need to decode it.
                    String referrer = URLDecoder.decode(rawReferrer, "UTF-8");

                    // Log the referrer string.
                    Log.d(Seeds.TAG, "Referrer: " + referrer);

                    String parts[] = referrer.split("&");
                    String cid = null;
                    String uid = null;
                    for(int i = 0; i < parts.length; i++){
                        if(parts[i].startsWith("countly_cid"))
                            cid = parts[i].replace("countly_cid=", "").trim();
                        if(parts[i].startsWith("countly_cuid"))
                            uid = parts[i].replace("countly_cuid=", "").trim();
                    }
                    String res = "";
                    if(cid != null)
                        res += "&campaign_id="+cid;
                    if(uid != null)
                        res += "&campaign_user="+uid;

                    Log.d(Seeds.TAG, "Processed: " + res);
                    // Persist the referrer string.
                    if(!res.equals(""))
                        context.getSharedPreferences(key, Context.MODE_PRIVATE).edit().putString(key, res).commit();
                }
            }
        }
        catch (Exception e)
        {
            Log.d(Seeds.TAG, e.toString());
        }
    }
}