package com.habitrpg.android.habitica.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class RemoteConfigManager {

    private Context context;
    private Boolean enableRepeatbles = false;
    private Boolean enableNewShops = false;
    private String shopSpriteSuffix = "";
    private Integer maxChatLength = 3000;
    private Boolean enableUsernameRelease = false;
    private String REMOTE_STRING_KEY = "remote-string";

    public RemoteConfigManager(Context context) {
        this.context = context;
        loadFromPreferences();
        new DownloadFileFromURL().execute("https://s3.amazonaws.com/habitica-assets/mobileApp/endpoint/config-android.json");
    }

    public Boolean repeatablesAreEnabled() {
        return enableRepeatbles;
    }

    public Boolean newShopsEnabled() {
        return enableNewShops;
    }

    public String shopSpriteSuffix() {
        return shopSpriteSuffix;
    }

    public Integer maxChatLength() { return maxChatLength; }

    public Boolean enableUsernameRelease() { return enableUsernameRelease; }

    private void loadFromPreferences () {
        String storedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(REMOTE_STRING_KEY, "");

        if (storedPreferences.isEmpty()) {
            return;
        }

        parseConfig(storedPreferences);
    }

    private void parseConfig(String jsonString) {
        try {
            JSONObject obj = new JSONObject(jsonString);
            enableRepeatbles = obj.getBoolean("enableRepeatables");
            if (obj.has("enableNewShops")) {
                enableNewShops = obj.getBoolean("enableNewShops");
            }
            if (obj.has("shopSpriteSuffix")) {
                shopSpriteSuffix = obj.getString("shopSpriteSuffix");
            }
            if (obj.has("maxChatLength")) {
                maxChatLength = obj.getInt("maxChatLength");
            }
            if (obj.has("enableUsernameRelease")) {
                enableUsernameRelease = obj.getBoolean("enableUsernameRelease");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class DownloadFileFromURL extends AsyncTask<String, String, String> {
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

            parseConfig(text.toString());
        }

    }
}
