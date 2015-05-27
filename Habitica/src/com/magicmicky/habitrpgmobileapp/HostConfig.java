package com.magicmicky.habitrpgmobileapp;
import java.lang.String; /**
 * The configuration of the host<br />
 * Currently, the Port isn't used at all.
 * @author MagicMicky
 */
public class HostConfig {
    private String address;
    private String port;
    private String api;
    private String user;
    /**
     * Create a new HostConfig
     * @param address the address of the server
     * @param port the port of the server
     * @param api the API token of the user
     * @param user the user ID
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
}

