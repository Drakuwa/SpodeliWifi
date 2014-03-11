package com.app.wifipass.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Milena on 2/25/14.
 */
public class Utils {

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static boolean isServerReachable() {
        URL url = null;
        try {
            url = new URL("http://spodeliwifi.apphb.com/Wifi.svc");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection conn = null;
        try {
            conn = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(3000);
        try {
            conn.getInputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Long getStringFromPrefs(Context context, String key) {
        return context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).getLong(key, 0);
    }

    public static void setLongToPrefs(Context context, String key, Long value) {
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE).edit().putLong(key, value).commit();
    }

    public static Typeface getTypeface(Context context) {
        return Typeface.createFromAsset(context.getAssets(),
                "fonts/Raleway-Regular.ttf");
    }
}
