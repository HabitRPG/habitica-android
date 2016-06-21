package com.magicmicky.habitrpgwrapper.lib.api;

/**
 * Created by MagicMicky on 15/06/2014.
 */
public class Server {
    private String addr;

    public Server(String addr) {
        this(addr, true);
    }

    public Server(String addr, boolean attachSuffix) {
        if (attachSuffix) {
            if (addr.endsWith("/api/v3") || addr.endsWith("/api/v3/"))
                this.addr = addr;
            else if (addr.endsWith("/"))
                this.addr = addr + "api/v3/";
            else
                this.addr = addr + "/api/v3/";
        } else {
            this.addr = addr;
        }
    }

    public Server(Server s) {
        this.addr = s.toString();
    }

    @Override
    public String toString() {
        return this.addr;
    }
}
