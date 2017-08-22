package com.habitrpg.android.habitica.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.habitrpg.android.habitica.BuildConfig;
import com.habitrpg.android.habitica.R;

/**
 * The configuration of the host<br />
 * Currently, the Port isn't used at all.
 *
 * @author MagicMicky
 */
public class HostConfig {
    private String address;
    private String port;
    private String api;
    private String user;

    public HostConfig(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.port = BuildConfig.PORT;
        this.address = BuildConfig.DEBUG ? BuildConfig.BASE_URL : context.getString(R.string.base_url);
        if (prefs.contains("base_url"))
            this.address = prefs.getString("base_url", context.getString(R.string.base_url));
        this.api = prefs.getString("APIToken", null);
        this.user = prefs.getString(context.getString(R.string.SP_userID), null);
    }

    /**
     * Create a new HostConfig
     *
     * @param address the address of the server
     * @param port    the port of the server
     * @param api     the API token of the user
     * @param user    the user ID
     */
    public HostConfig(String address, String port, String api, String user) {
        this.setAddress(address);
        this.setPort(port);
        this.setApi(api);
        this.setUser(user);
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the api
     */
    public String getApi() {
        return api;
    }

    /**
     * @param api the api to set
     */
    public void setApi(String api) {
        this.api = api;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    public boolean hasAuthentication() {
        return user != null && user.length() > 0 && api != null && api.length() > 0;
    }
}

