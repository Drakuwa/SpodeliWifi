package com.app.wifipass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.app.wifipass.adapters.AdapterShowAllAps;
import com.app.wifipass.databases.DatabaseAccessPoints;
import com.app.wifipass.pojos.AccessPoint;
import com.app.wifipass.utils.Constants;
import com.app.wifipass.utils.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

/**
 * Created by Milena on 2/24/14.
 */
public class MainActivity extends ActionBarActivity {
    private WifiManager mWiFiManager;
    private View mProgressBarView;
    private DatabaseAccessPoints mAccessPointDatabase;
    private ArrayList<AccessPoint> mAvailableApS;
    private WifiInfo mWifiInfo;
    private ListView mListViewMatched;
    private AdapterShowAllAps adapter;
    private boolean isWEP = false;
    private TextView scan;
    private ArrayList<AccessPoint> mArrayAllAccessPoints;
    private int netId;
    private WifiReceiver mBroadcastReceiverWifiChanges;
    private IntentFilter intentFilter;
    private String alreadyConfigured;
    private String incorrectApOrPassword;
    private String wifiConnected;
    private String wifiDisconnected;
    private String waitUntilWifiIsEnabled;
    private String turnOnNetworkConnection;
    private String connectionToServerFailed;
    private String noAccessPointsAvailable;
    private String noAps;
    private String accessPointAdded;
    private String failedToAddAccessPoint;
    private String wifiPasswordSaved;

    private boolean isDialogOpened;
    private boolean isEnableDialogOpened;
    private Crouton crouton;
    private Crouton croutonEnableWifi;
    private Crouton croutonProgressBar;

    private Style confirmStyle;
    private Style alertStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setIcon(R.drawable.ic_logo);

        mAccessPointDatabase = new DatabaseAccessPoints(this);

        isDialogOpened = false;
        isEnableDialogOpened = false;

        alreadyConfigured = this.getString(R.string.already_configured);
        incorrectApOrPassword = this.getString(R.string.incorrect_ap_or_password);
        wifiConnected = this.getString(R.string.wifi_connected);
        wifiDisconnected = this.getString(R.string.wifi_disconnected);
        waitUntilWifiIsEnabled = this.getString(R.string.wait_until_wifi_is_enabled);
        turnOnNetworkConnection = this.getString(R.string.turn_on_network_connection);
        connectionToServerFailed = this.getString(R.string.connection_to_server_failed);
        noAccessPointsAvailable = this.getString(R.string.no_access_points_available);
        noAps = this.getString(R.string.no_aps);
        accessPointAdded = this.getString(R.string.access_point_added);
        failedToAddAccessPoint = this.getString(R.string.failed_to_add_access_point);
        wifiPasswordSaved = this.getString(R.string.wifi_password_remembered);

        confirmStyle = new Style.Builder()
                .setBackgroundColor(R.color.confirm_color)
                .setTextColor(R.color.white_color)
                .build();

        alertStyle = new Style.Builder()
                .setBackgroundColor(R.color.alert_color)
                .setTextColor(R.color.white_color)
                .build();

        mWiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scan = (TextView) this.findViewById(R.id.textviewScan);

        mListViewMatched = (ListView) this.findViewById(R.id.listMatched);
        if (android.os.Build.VERSION.SDK_INT >= 9)
            mListViewMatched.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mListViewMatched.setOnScrollListener(new AbsListView.OnScrollListener() {
            int prevVisibleItem = 0;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == totalItemCount) {
                    ((ViewGroup.MarginLayoutParams) mListViewMatched.getLayoutParams()).bottomMargin = scan.getHeight();
                    mListViewMatched.requestLayout();
                } else {
                    ((ViewGroup.MarginLayoutParams) mListViewMatched.getLayoutParams()).bottomMargin = 0;
                    mListViewMatched.requestLayout();
                    if (prevVisibleItem != firstVisibleItem) {
                        if (prevVisibleItem < firstVisibleItem) {
                            //ScrollDown
                            TextView textViewScan = (TextView) MainActivity.this.findViewById(R.id.textviewScan);
                            if (textViewScan.getVisibility() == View.VISIBLE) {
                                Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                                textViewScan.startAnimation(slideDown);
                                textViewScan.setVisibility(View.INVISIBLE);
                            }
                        } else {
                            //ScrollUp
                            TextView textViewScan = (TextView) MainActivity.this.findViewById(R.id.textviewScan);
                            if (textViewScan.getVisibility() == View.INVISIBLE) {
                                Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                                textViewScan.startAnimation(slideUp);
                                textViewScan.setVisibility(View.VISIBLE);
                            }
                        }
                        prevVisibleItem = firstVisibleItem;
                    }
                }
            }
        });

        Typeface typeface = Utils.getTypeface(this);
        scan.setTypeface(typeface);

        Long lastUpdate = Utils.getStringFromPrefs(this, Constants.LAST_UPDATE);
        Long weeklyUpdate = Utils.getStringFromPrefs(this, Constants.WEEKLY_UPDATE);
        Long currentDate = System.currentTimeMillis();
        Long timeDifference = currentDate - lastUpdate;
        Long timeDifference1 = currentDate - weeklyUpdate;
        long day = 24 * 60 * 60 * 1000;
        long week = day * 7;

        if (weeklyUpdate != 0 && timeDifference1 >= week)
            dropDatabase();
        else {
            if (timeDifference >= day)
                sync();
            else
                scan();
        }

        scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scan();
            }
        });

        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(ConnectivityManager.EXTRA_REASON);

        mBroadcastReceiverWifiChanges = new WifiReceiver();
        registerReceiver(mBroadcastReceiverWifiChanges, intentFilter);
    }

    @Override
    protected void onPause() {
        MainActivity.this.unregisterReceiver(mBroadcastReceiverWifiChanges);
        Crouton.cancelAllCroutons();
        super.onPause();
    }

    @Override
    protected void onResume() {
        intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(ConnectivityManager.EXTRA_REASON);
        registerReceiver(mBroadcastReceiverWifiChanges, intentFilter);
        super.onResume();
    }

    public void dropDatabase() {
        mAccessPointDatabase.dropDatabase();
        Utils.setLongToPrefs(MainActivity.this, Constants.WEEKLY_UPDATE, System.currentTimeMillis());
        sync();
    }

    public void scan() {
        if (mWiFiManager.isWifiEnabled()) {
            new isScanning().execute();
        } else if (mWiFiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            Crouton.cancelAllCroutons();
            Crouton.makeText(MainActivity.this,
                    waitUntilWifiIsEnabled, confirmStyle)
                    .show();
        } else if (mWiFiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING
                || mWiFiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            noInternetConnection();
        }
    }

    public void sync() {
        if (Utils.isOnline(this)) {
            String param = mAccessPointDatabase.getLastAccessPointId();
            GetAccessPointsFromServer mSyncTask = new GetAccessPointsFromServer();
            mSyncTask.execute(param);
        } else {
            Crouton.cancelAllCroutons();
            Crouton.makeText(MainActivity.this,
                    turnOnNetworkConnection, confirmStyle)
                    .show();
        }
    }

    private ArrayList<AccessPoint> parseJSON(String entityString) {
        ArrayList<AccessPoint> mAccessPointsFromServer;
        Gson gson = new Gson();
        mAccessPointsFromServer = gson.fromJson(entityString, new TypeToken<ArrayList<AccessPoint>>() {
        }.getType());

        if (mAccessPointDatabase != null)
            mAccessPointDatabase.addAllAccessPoints(mAccessPointsFromServer);
        else {
            Crouton.cancelAllCroutons();
            Crouton.makeText(MainActivity.this,
                    noAccessPointsAvailable, alertStyle)
                    .show();
        }
        return mAccessPointsFromServer;
    }

    public void noInternetConnection() {
        if (isEnableDialogOpened) {
            Crouton.cancelAllCroutons();
            isEnableDialogOpened = false;
            return;
        }
        isEnableDialogOpened = true;
        View mDialogEnableWifi = getLayoutInflater().inflate(R.layout.enable_wifi_dialog, null);

        Crouton.cancelAllCroutons();
        croutonEnableWifi = Crouton.make(this, mDialogEnableWifi)
                .setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());
        croutonEnableWifi.show();

        Typeface typeface = Utils.getTypeface(this);
        TextView enableWifiText = (TextView) mDialogEnableWifi.findViewById(R.id.text_enable_wifi);
        enableWifiText.setTypeface(typeface);

        TextView enableWifiTextView = (TextView) mDialogEnableWifi.findViewById(R.id.text_view_enable_wifi);
        enableWifiTextView.setTypeface(typeface);
        enableWifiTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWiFiManager.setWifiEnabled(true);
                Crouton.hide(croutonEnableWifi);
                isEnableDialogOpened = false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_item_add_new:
                if (Utils.isOnline(this)) {
                    addAccessPoint();
                } else {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            turnOnNetworkConnection, alertStyle)
                            .show();
                }
                return true;

            case R.id.menu_item_sync:
                if (Utils.isOnline(this)) {
                    String param = mAccessPointDatabase.getLastAccessPointId();
                    GetAccessPointsFromServer mSyncTask = new GetAccessPointsFromServer();
                    mSyncTask.execute(param);
                } else {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            turnOnNetworkConnection, alertStyle)
                            .show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getAvailableAPs() {
        mWiFiManager.startScan();
        List<ScanResult> mScanResults = mWiFiManager.getScanResults();
        mAvailableApS = new ArrayList<AccessPoint>();
        if (mScanResults != null) {
            for (ScanResult mScanResult : mScanResults) {
                AccessPoint mAvailableAP = new AccessPoint();
                String apname = mScanResult.SSID.toUpperCase();
                String bssid = mScanResult.BSSID;
                mAvailableAP.setName(apname);
                mAvailableAP.setBssid(bssid);
                mAvailableApS.add(mAvailableAP);
            }
        }
    }

    public void getAllAccessPoints() {
        mAccessPointDatabase = new DatabaseAccessPoints(this);
        mArrayAllAccessPoints = new ArrayList<AccessPoint>();
        mArrayAllAccessPoints = mAccessPointDatabase.getAllAps();
    }

    public ArrayList<AccessPoint> getSavedAPs() {
        ArrayList<AccessPoint> mMatchedAccessPointsArrayList = new ArrayList<AccessPoint>();
        if ((mAvailableApS.size() > 0) && (mArrayAllAccessPoints.size() > 0)) {
            for (int i = 0; i < mAvailableApS.size(); i++) {
                for (int j = 0; j < mArrayAllAccessPoints.size(); j++) {
                    AccessPoint currentAP = mAvailableApS.get(i);
                    AccessPoint accessPoint = mArrayAllAccessPoints.get(j);
                    if (currentAP.getBssid().equals(accessPoint.getBssid())) {
                        if ((mAccessPointDatabase.getLastAccessPointId(currentAP.getBssid())) == (accessPoint.getId()))
                            accessPoint.setLast(true);
                        else
                            accessPoint.setLast(false);

                        mMatchedAccessPointsArrayList.add(accessPoint);
                    }
                }
            }
        }
        return mMatchedAccessPointsArrayList;
    }

    public void connectTo(AccessPoint accessPoint) {
        boolean exists = false;

        String mApName = accessPoint.getName();
        String mPass = accessPoint.getPassword();
        String mBssid = accessPoint.getBssid();

        // List available networks
        List<WifiConfiguration> configs = mWiFiManager.getConfiguredNetworks();
        for (WifiConfiguration config : configs) {
            if (config.SSID.equalsIgnoreCase("\"" + mApName + "\"")) {
                exists = true;
            }
        }

        if (!exists) {
            WifiConfiguration wifiConfig = new WifiConfiguration();
            wifiConfig.SSID = "\"" + mApName + "\"";
            wifiConfig.BSSID = mBssid;
            if (isWEP) {
                wifiConfig.wepKeys[0] = "\"" + mPass + "\"";
            } else
                wifiConfig.preSharedKey = "\"" + mPass + "\"";
            wifiConfig.status = WifiConfiguration.Status.ENABLED;

            mWiFiManager.setWifiEnabled(true);
            netId = mWiFiManager.addNetwork(wifiConfig);
            if (netId == -1) {
                Crouton.cancelAllCroutons();
                Crouton.makeText(this,
                        incorrectApOrPassword, alertStyle)
                        .show();
            } else {
                Crouton.cancelAllCroutons();
                Crouton.makeText(this,
                        wifiPasswordSaved, confirmStyle)
                        .show();
                mWiFiManager.enableNetwork(netId, true);
                MainActivity.this.registerReceiver(mBroadcastReceiverWifiChanges, intentFilter);
            }

        } else {
            Crouton.cancelAllCroutons();
            Crouton.makeText(this,
                    alreadyConfigured, alertStyle)
                    .show();
        }
    }

    public void addAccessPoint() {
        View mAlertDialogView = getLayoutInflater().inflate(R.layout.add_dialog, null);
        TextView textViewScan = (TextView) MainActivity.this.findViewById(R.id.textviewScan);
        final EditText name = (EditText) mAlertDialogView.findViewById(R.id.apn);
        final EditText pass = (EditText) mAlertDialogView.findViewById(R.id.password);

        if (isDialogOpened) {
            Crouton.hide(crouton);
            isDialogOpened = false;
            Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
            textViewScan.startAnimation(slideUp);
            textViewScan.setVisibility(View.VISIBLE);
            return;
        }

        isDialogOpened = true;
        mWifiInfo = mWiFiManager.getConnectionInfo();

        Crouton.cancelAllCroutons();
        crouton = Crouton.make(this, mAlertDialogView)
                .setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());
        crouton.show();

        if (textViewScan.getVisibility() == View.VISIBLE) {
            Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
            textViewScan.startAnimation(slideDown);
            textViewScan.setVisibility(View.INVISIBLE);
        }

        Typeface typeface = Utils.getTypeface(this);

        name.setText(mWifiInfo.getSSID());
        name.setTypeface(typeface);

        pass.setTypeface(typeface);

        TextView saveTextView = (TextView) mAlertDialogView.findViewById(R.id.save);
        saveTextView.setTypeface(typeface);
        saveTextView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                String mApName = name.getText().toString();
                                                String mPassword = pass.getText().toString();
                                                String mBssid = mWifiInfo.getBSSID();
                                                if (mBssid != null) {
                                                    String mChangedBssid = mBssid;
                                                    mChangedBssid = mChangedBssid.replace(":", "_");
                                                    mApName = mApName.replace("\"", "");
                                                    String param = mApName.replace(" ", "%20")
                                                            + "/" + mPassword.replace(" ", "%20")
                                                            + "/" + mChangedBssid;
                                                    AddAccessPointToServer mAdd = new AddAccessPointToServer();
                                                    mAdd.execute(param);
                                                } else {
                                                    Crouton.hide(crouton);
                                                    Crouton.cancelAllCroutons();
                                                    Crouton.makeText(MainActivity.this,
                                                            failedToAddAccessPoint, alertStyle)
                                                            .show();
                                                }

                                                TextView textViewScan = (TextView) MainActivity.this.findViewById(R.id.textviewScan);
                                                isDialogOpened = false;

                                                Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                                                textViewScan.startAnimation(slideUp);
                                                textViewScan.setVisibility(View.VISIBLE);
                                            }
                                        }
        );

        TextView cancelTextView = (TextView) mAlertDialogView.findViewById(R.id.cancel);
        cancelTextView.setTypeface(typeface);
        cancelTextView.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View view) {
                                                  TextView textViewScan = (TextView) MainActivity.this.findViewById(R.id.textviewScan);
                                                  isDialogOpened = false;
                                                  Crouton.hide(crouton);
                                                  Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                                                  textViewScan.startAnimation(slideUp);
                                                  textViewScan.setVisibility(View.VISIBLE);
                                              }
                                          }
        );
    }

    public class GetAccessPointsFromServer extends
            AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            mProgressBarView = getLayoutInflater().inflate(R.layout.progress_bar, null);
            Crouton.cancelAllCroutons();
            croutonProgressBar = Crouton.make(MainActivity.this, mProgressBarView)
                    .setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());
            croutonProgressBar.show();
            super.onPreExecute();
        }

        @Override
        protected String[] doInBackground(String... param) {
            String uri = Constants.URL_SERVER_GET + param[0];
            String[] result = new String[2];
            result[0] = param[0];
            HttpEntity responseEntity = null;
            if (Utils.isServerReachable()) {
                try {
                    // send GET to the server for featured products
                    HttpGet request = new HttpGet(uri);

                    // inform the server we want json object
                    request.setHeader("Accept", "application/json");
                    request.setHeader("Content-type", "application/json");

                    // execute GET method
                    DefaultHttpClient httpClient = new DefaultHttpClient();

                    HttpResponse response = httpClient.execute(request);

                    // get the response
                    responseEntity = response.getEntity();

                    // get the json string
                    result[1] = EntityUtils.toString(responseEntity);
                    if (result[1] != null) {
                        parseJSON(result[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            Crouton.hide(croutonProgressBar);

            if (result[1] == null) {
                Crouton.cancelAllCroutons();
                Crouton.makeText(MainActivity.this,
                        connectionToServerFailed, alertStyle)
                        .show();
            } else {
                Utils.setLongToPrefs(MainActivity.this, Constants.LAST_UPDATE, System.currentTimeMillis());

                if (mWiFiManager.isWifiEnabled())
                    new isScanning().execute();
                else if (mWiFiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            waitUntilWifiIsEnabled, confirmStyle)
                            .show();
                } else if (mWiFiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING
                        || mWiFiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
                    noInternetConnection();
            }
        }
    }

    public class isScanning extends AsyncTask<String, Void, ArrayList<AccessPoint>> {
        @Override
        protected void onPreExecute() {
            mProgressBarView = getLayoutInflater().inflate(R.layout.progress_bar, null);
            Crouton.cancelAllCroutons();
            croutonProgressBar = Crouton.make(MainActivity.this, mProgressBarView)
                    .setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());
            croutonProgressBar.show();
            super.onPreExecute();
        }

        @Override
        protected ArrayList<AccessPoint> doInBackground(String... params) {
            ArrayList<AccessPoint> result = new ArrayList<AccessPoint>();
            if (mWiFiManager.isWifiEnabled()) {
                getAllAccessPoints();
                getAvailableAPs();
                result = getSavedAPs();
            }
            return result;
        }

        public void onPostExecute(ArrayList<AccessPoint> result) {
            Crouton.hide(croutonProgressBar);
            if (result != null) {
                adapter = new AdapterShowAllAps(MainActivity.this, result);
                mListViewMatched.setAdapter(adapter);

                mListViewMatched.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> arg0,
                                            View arg1, int arg2, long arg3) {
                        AccessPoint tempAccessPoint = adapter.getItem(arg2);
                        connectTo(tempAccessPoint);
                    }
                });

            } else {
                Crouton.cancelAllCroutons();
                Crouton.makeText(MainActivity.this,
                        noAps, alertStyle)
                        .show();
            }
        }
    }

    private class AddAccessPointToServer extends
            AsyncTask<String, Integer, String[]> {

        @Override
        protected void onPreExecute() {
            mProgressBarView = getLayoutInflater().inflate(R.layout.progress_bar, null);
            Crouton.cancelAllCroutons();
            croutonProgressBar = Crouton.make(MainActivity.this, mProgressBarView)
                    .setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_INFINITE).build());
            croutonProgressBar.show();
        }

        @Override
        protected String[] doInBackground(String... param) {

            String uri = Constants.URL_SERVER_ADD + param[0];
            String[] result = new String[2];
            result[0] = param[0];

            try {
                // send GET to the server for featured products
                HttpGet request = new HttpGet(uri);
                // inform the server we want json object
                request.setHeader("Accept", "application/json");
                request.setHeader("Content-type", "application/json");

                DefaultHttpClient httpClient = new DefaultHttpClient();

                HttpResponse response = httpClient.execute(request);
                HttpEntity responseEntity = response.getEntity();
                result[1] = EntityUtils.toString(responseEntity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
            Crouton.hide(croutonProgressBar);
            if (result[1] != null) {
                if (result[1].equalsIgnoreCase("true")) {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            accessPointAdded, confirmStyle)
                            .show();
                } else {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            failedToAddAccessPoint, alertStyle)
                            .show();
                }
            } else {
                Crouton.cancelAllCroutons();
                Crouton.makeText(MainActivity.this,
                        failedToAddAccessPoint, alertStyle)
                        .show();
            }
            super.onPostExecute(result);
        }
    }


    private class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent
                    .getStringExtra(ConnectivityManager.EXTRA_REASON);
            NetworkInfo currentNetworkInfo = (NetworkInfo) intent
                    .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            String state = currentNetworkInfo.getDetailedState().name();

            // CONNECTED or DISCONNECTED
            if (currentNetworkInfo.getTypeName().equalsIgnoreCase("WIFI")) {
                if (currentNetworkInfo.isConnected()) {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            wifiConnected, confirmStyle)
                            .show();

                    // If the phone has successfully connected to the AP, save it!
                    mWiFiManager.saveConfiguration();
                } else if (reason != null) {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            reason, alertStyle)
                            .show();
                } else if (state.equalsIgnoreCase("DISCONNECTED")) {
                    Crouton.cancelAllCroutons();
                    Crouton.makeText(MainActivity.this,
                            wifiDisconnected, alertStyle)
                            .show();
                    mWiFiManager.removeNetwork(netId);
                }
            }
        }
    }
}

