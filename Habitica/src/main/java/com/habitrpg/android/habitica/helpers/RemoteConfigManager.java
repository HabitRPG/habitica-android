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

    private static RemoteConfigManager instance;
    private Context context;
    private static Boolean enableRepeatbles = false;
    private String REMOTE_STRING_KEY = "remote-string";

    private RemoteConfigManager(Context context) {
        this.context = context;
        loadFromPreferences();
        new DownloadFileFromURL().execute("https://s3.amazonaws.com/habitica-assets/mobileApp/endpoint/config-ios.json");
    }

    public static RemoteConfigManager getInstance(Context context) {
        if (instance == null) {
            instance = new RemoteConfigManager(context);
        }
        return instance;
    }

    public static Boolean repeatablesAreEnabled () {
        return enableRepeatbles;
    }

    private void loadFromPreferences () {
        String storedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(REMOTE_STRING_KEY, "");

        if (storedPreferences.isEmpty()) {
            return;
        }

        try {
            JSONObject obj = new JSONObject(storedPreferences);
            enableRepeatbles = obj.getBoolean("enableRepeatbles");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        private String filename = "config.json";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String ...fileUrl) {
            int count;
            try {
                URL url = new URL(fileUrl[0]);
                URLConnection conection = url.openConnection();
                conection.connect();

                int lenghtOfFile = conection.getContentLength();

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
                enableRepeatbles = obj.getBoolean("enableRepeatbles");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
