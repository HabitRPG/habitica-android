package com.habitrpg.android.habitica.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import bolts.Bolts;

/**
 * Created by keith holliday on 4/7/2017.
 */

public class RemoteConfigManager {

  //  private static RemoteConfigManager instance; //This reports a leak in leak canary, it is possible that we don't need a singleton as this class loads settings on construction into preferences. It is the only use it is never used after this again as the only other public method is static
    private static Boolean enableRepeatbles = false;
    private static String REMOTE_STRING_KEY = "remote-string";

    public static void loadConfig(Context context) {
        loadFromPreferences(context);
        new DownloadFileFromURL().execute(context, "https://s3.amazonaws.com/habitica-assets/mobileApp/endpoint/config-android.json");
    }

//    public static RemoteConfigManager getInstance(Context context) {
//        if (instance == null) {
//            instance = new RemoteConfigManager(context);
//        }
//        return instance;
//    }

    public static Boolean repeatablesAreEnabled () {
        return enableRepeatbles;
    }

    private static void loadFromPreferences(Context context) {
        String storedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(REMOTE_STRING_KEY, "");

        if (storedPreferences.isEmpty()) {
            return;
        }

        try {
            JSONObject obj = new JSONObject(storedPreferences);
            enableRepeatbles = obj.getBoolean("enableRepeatables");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    static class DownloadFileFromURL extends AsyncTask<Object, String, String> {
        private String filename = "config.json";
        private Context context;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Object... params) {
            context = (Context) params[0];
            String fileUrl = (String) params[1];

            int count;
            try {
                URL url = new URL(fileUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lenghtOfFile = connection.getContentLength();

                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                OutputStream output = context.openFileOutput(filename, Context.MODE_PRIVATE);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();

                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            File file = new File(context.getFilesDir(), filename);
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit().putString(REMOTE_STRING_KEY, text.toString()).apply();

            try {
                JSONObject obj = new JSONObject(text.toString());
                enableRepeatbles = obj.getBoolean("enableRepeatables");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            context = null;
        }

    }
}
