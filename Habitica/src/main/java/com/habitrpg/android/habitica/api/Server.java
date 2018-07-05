package com.habitrpg.android.habitica.api;

/**
 * Created by MagicMicky on 15/06/2014.
 */
public class Server {
    private String addr;

    public Server(String addr) {
        this(addr, true);
    }

    private Server(String addr, boolean attachSuffix) {
        if (attachSuffix) {
            if (addr.endsWith("/api/v4") || addr.endsWith("/api/v4/")) {
                this.addr = addr;
            } else if (addr.endsWith("/")) {
                this.addr = addr + "api/v4/";
            } else {
                this.addr = addr + "/api/v4/";
            }
        } else {
            this.addr = addr;
        }
    }

    public Server(Server server) {
        this.addr = server.toString();
    }

    @Override
    public String toString() {
        return this.addr;
    }
}
