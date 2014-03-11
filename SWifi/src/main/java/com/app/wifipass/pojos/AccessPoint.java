package com.app.wifipass.pojos;

/**
 * Created by Milena on 2/24/14.
 */
public class AccessPoint {

    private int id;
    private String name;
    private String password;
    private String bssid;
    private boolean last;

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean isLast) {
        this.last = isLast;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid;
    }
}
