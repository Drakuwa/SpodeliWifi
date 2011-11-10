package com.app.wifipass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class WiFiPassShareActivity extends Activity {

	private WifiManager mWiFiManager;
	private ListView lv;
	private ArrayAdapter<String> adapter;
	private ArrayList<String> AP = new ArrayList<String>();
	private ArrayList<String> savedAP = new ArrayList<String>();
	private ArrayList<String> matchingAP = new ArrayList<String>();
	private static final int MENU_SYNC = Menu.FIRST;
	private static final int MENU_ADD = MENU_SYNC + 1;
	private static final int MENU_ALL = MENU_ADD + 1;
	private String version = "-1";
	private String newversion;
	private String fullap;
	private File path;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		path = new File(Environment.getExternalStorageDirectory(), this
				.getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}
		File versionfile = new File(path, "version");
		if (!versionfile.exists()) {
			try {
				versionfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File collectionfile = new File(path, "collection");
		if (!collectionfile.exists()) {
			try {
				collectionfile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					versionfile));
			String sResponse;
			StringBuilder s = new StringBuilder();
			while ((sResponse = bufferedReader.readLine()) != null) {
				s = s.append(sResponse);
			}
			Log.d("xxx", s.toString());
			if (s.toString().length() > 0)
				version = s.toString();

			/*
			 * BufferedReader bufferedReader2 = new BufferedReader(new
			 * FileReader( collectionfile)); String sResponse2; StringBuilder s2
			 * = new StringBuilder(); while ((sResponse2 =
			 * bufferedReader2.readLine()) != null) { s2 =
			 * s2.append(sResponse2+"\n"); } Log.d("xxx", s2.toString());
			 */
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		mWiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// WifiInfo w = mWiFiManager.getConnectionInfo();

		Button scan = (Button) findViewById(R.id.scan);
		scan.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mWiFiManager.isWifiEnabled()) {
					while(mWiFiManager.getWifiState()!=WifiManager.WIFI_STATE_ENABLED){
						Log.d("xxx", "Please wait...");
					}
					getAvailableAPs();
					getSavedAPs();
				} else
					Toast.makeText(getApplicationContext(),
							"Please enable Your WiFi!", Toast.LENGTH_SHORT)
							.show();
			}
		});

		// Toast.makeText(this, "APN Name = "+w.getSSID(),
		// Toast.LENGTH_SHORT).show();
		/*
		 * final BroadcastReceiver mWiFiBroadcastReceiver1 = new
		 * BroadcastReceiver() {
		 * 
		 * @Override public void onReceive(Context context, Intent intent) {
		 * List<ScanResult> mScanResults = mWiFiManager.getScanResults();
		 * StringBuilder sb = new StringBuilder(); for (ScanResult sr :
		 * mScanResults) { sb.append("ACCESS POINT NAME: " + sr.SSID);
		 * sb.append("\n"); sb.append("BSSID: " + sr.BSSID); sb.append("\n");
		 * sb.append("SIGNAL: " + sr.level); sb.append("\n"); } info =
		 * sb.toString(); } }; IntentFilter intentFilter = null;
		 * registerReceiver(mWiFiBroadcastReceiver1, intentFilter);
		 */

		/*
		 * try { File psk = new File("/mnt/sdcard/wpa_supplicant.conf"); //
		 * FileInputStream inputStream = //
		 * openFileInput("/data/misc/wifi/wpa_supplicant.conf"); //
		 * InputStreamReader inputStreamReader = new //
		 * InputStreamReader(inputStream); BufferedReader bufferedReader = new
		 * BufferedReader(new FileReader( psk)); String sResponse; StringBuilder
		 * s = new StringBuilder();
		 * 
		 * while ((sResponse = bufferedReader.readLine()) != null) { s =
		 * s.append(sResponse); } Log.d("xxx", s.toString()); } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * ioe) { ioe.printStackTrace(); }
		 */
	}

	public boolean HaveNetworkConnection() {
		boolean HaveConnectedWifi = false;
		boolean HaveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					HaveConnectedWifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					HaveConnectedMobile = true;
		}
		return HaveConnectedWifi || HaveConnectedMobile;
	}

	public void createInternetDisabledAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder
				.setMessage(
						"Your internet connection is disabled! Please enable WiFi, or mobile internet")
				.setIcon(R.drawable.icon).setTitle(R.string.app_name)
				.setCancelable(false).setPositiveButton("Internet options",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								showNetOptions();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void showNetOptions() {
		Intent netOptionsIntent = new Intent(
				android.provider.Settings.ACTION_WIRELESS_SETTINGS);
		this.startActivity(netOptionsIntent);
	}

	public void getAvailableAPs() {

		AP.clear();
		mWiFiManager.startScan();
		List<ScanResult> mScanResults = mWiFiManager.getScanResults();
		for (ScanResult sr : mScanResults) {
			Log.d("xxx", "Scan results: " + sr.toString());
			StringBuilder sb = new StringBuilder();
			sb.append("AP NAME: " + sr.SSID);
			sb.append("\n");
			sb.append("BSSID: " + sr.BSSID);
			// sb.append("\n");
			// sb.append("SIGNAL: " + sr.level);
			AP.add(sb.toString());
		}
	}

	public void getSavedAPs() {

		savedAP.clear();
		matchingAP.clear();
		File collectionfile = new File(path, "collection");
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader(collectionfile));
			String sResponse;
			while ((sResponse = bufferedReader.readLine()) != null) {
				savedAP.add(sResponse);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < AP.size(); i++) {
			for (int j = 0; j < savedAP.size(); j++) {
				String currentAP = AP.get(i);
				String bssid = "";
				String localbssid = "";
				if (currentAP.contains("BSSID:")) {
					bssid = currentAP
							.substring(currentAP.indexOf("BSSID:") + 7);
					Log.d("xxx", "YES! it contains -> " + bssid);
				} else
					continue;
				String localsavedAP = savedAP.get(j);
				Log.d("xxx", "local saved ap: " + localsavedAP);
				if (localsavedAP.contains("*")) {
					localbssid = localsavedAP.substring(localsavedAP
							.indexOf("*") + 1);
					Log.d("xxx", "YES again! it contains -> " + localbssid);
				} else
					continue;
				Log.d("xxx", bssid + "?=" + localbssid);

				if (bssid.equalsIgnoreCase(localbssid)) {
					StringBuilder sb = new StringBuilder();
					sb.append("AP name: "
							+ localsavedAP.substring(0, localsavedAP
									.indexOf(";")));
					sb.append("\n");
					sb.append("BSSID: " + bssid);
					sb.append("\n");
					sb.append("Password: "
							+ localsavedAP.substring(
									localsavedAP.indexOf(";") + 1, localsavedAP
											.indexOf("*")));
					matchingAP.add(sb.toString());
					Log.d("xxx", "match in: " + bssid + "=" + localbssid);
				}
			}
		}
		Log.d("xxx", "Ne stiga do ovde?");
		lv = (ListView) findViewById(R.id.accesspointslist);

		if (matchingAP.isEmpty()) {
			Toast.makeText(getApplicationContext(), "No matching APs!",
					Toast.LENGTH_SHORT).show();
		}
		adapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, matchingAP);
		lv.setAdapter(adapter);
		lv.setTextFilterEnabled(true);
		adapter.notifyDataSetChanged();

		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				connectTo(arg0.getAdapter().getItem(arg2).toString());
			}
		});
	}

	public void getAllAPs() {
		savedAP.clear();
		matchingAP.clear();
		File collectionfile = new File(path, "collection");
		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader(collectionfile));
			String sResponse;
			while ((sResponse = bufferedReader.readLine()) != null) {
				savedAP.add(sResponse);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < savedAP.size(); i++) {
			String localsavedAP = savedAP.get(i);
			if (localsavedAP.contains("*")) {
				String bssid = localsavedAP
						.substring(localsavedAP.indexOf("*") + 1);
				StringBuilder sb = new StringBuilder();
				sb.append("AP name: "
						+ localsavedAP.substring(0, localsavedAP.indexOf(";")));
				sb.append("\n");
				sb.append("BSSID: " + bssid);
				sb.append("\n");
				sb.append("Password: "
						+ localsavedAP.substring(localsavedAP.indexOf(";") + 1,
								localsavedAP.indexOf("*")));
				matchingAP.add(sb.toString());
			}
		}
		lv = (ListView) findViewById(R.id.accesspointslist);

		if (matchingAP.isEmpty()) {
			Toast.makeText(getApplicationContext(), "No APs!",
					Toast.LENGTH_SHORT).show();
		}
		adapter = new ArrayAdapter<String>(getApplicationContext(),
				android.R.layout.simple_list_item_1, matchingAP);
		lv.setAdapter(adapter);
		lv.setTextFilterEnabled(true);
		adapter.notifyDataSetChanged();
	}

	/**
	 * Initialize the options menu with the given labels and icons.
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu pMenu) {
		pMenu.add(0, MENU_SYNC, Menu.NONE, "Sync!").setIcon(
				android.R.drawable.ic_menu_rotate);
		pMenu.add(0, MENU_ADD, Menu.NONE, "Add New!").setIcon(
				android.R.drawable.ic_menu_send);
		pMenu.add(0, MENU_ALL, Menu.NONE, "Show all!").setIcon(
				android.R.drawable.ic_menu_sort_alphabetically);
		return true;
	}

	/**
	 * An override of the menu item select method that adds some useful
	 * functionalities to this part of the application.
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SYNC:
			if (HaveNetworkConnection()) {
				new sync().execute();
			} else
				createInternetDisabledAlert();
			return true;

		case MENU_ADD:
			if (HaveNetworkConnection()) {
				addAP();
			} else
				createInternetDisabledAlert();
			return true;

		case MENU_ALL:
			getAllAPs();
			return true;

		default:
		}
		return false;
	}

	public class sync extends AsyncTask<String, Void, String> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(WiFiPassShareActivity.this);
			dialog.setTitle("Syncing!");
			dialog.setMessage("Please wait..");
			dialog.setCancelable(false);
			dialog.setIndeterminate(true);
			dialog.show();
		}

		protected String doInBackground(String... vlezni) {
			String result = "";
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"http://drakuwa.admin.mk/test.php");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("command", "get"));
				nameValuePairs.add(new BasicNameValuePair("content", version));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = httpclient.execute(httppost);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));

				String sResponse;
				StringBuilder s = new StringBuilder();

				sResponse = reader.readLine();
				Log.d("xxx", "First row: " + sResponse);
				if (sResponse.substring(0, 8).equalsIgnoreCase("version:"))
					newversion = sResponse.substring(sResponse
							.indexOf("version: ") + 9);
				else if (sResponse.equalsIgnoreCase("same version"))
					return sResponse;
				while ((sResponse = reader.readLine()) != null) {
					s = s.append(sResponse + "\n");
				}
				Log.d("xxx", s.toString());
				result = s.toString();

			} catch (Exception e) {
				// handle exception here
				e.printStackTrace();
			}
			return result;
		}

		/**
		 * What to do after the calculations are finished.
		 */
		public void onPostExecute(String result) {
			// Remove the progress dialog.
			dialog.dismiss();
			if (result.equalsIgnoreCase("same version")) {
				Toast.makeText(getApplicationContext(), "Already up to date!",
						Toast.LENGTH_SHORT).show();
			} else if (result.startsWith("AP list:")) {
				try {
					File version = new File(path, "version");
					FileWriter fWriter = new FileWriter(version);
					fWriter.write(newversion);
					fWriter.flush();
					fWriter.close();

					File collection = new File(path, "collection");
					FileWriter fWriter2 = new FileWriter(collection);
					fWriter2.write(result);
					fWriter2.flush();
					fWriter2.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				Log.d("xxx", "New version: " + newversion + " old: " + version
						+ "rezultat: " + result);
				version = newversion;
			} else
				Toast.makeText(getApplicationContext(),
						"Connection error, try again!", Toast.LENGTH_SHORT)
						.show();
		}
	}

	public class add extends AsyncTask<String, Void, String> {
		ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(WiFiPassShareActivity.this);
			dialog.setTitle("Syncing!");
			dialog.setMessage("Please wait..");
			dialog.setCancelable(false);
			dialog.setIndeterminate(true);
			dialog.show();
		}

		protected String doInBackground(String... vlezni) {
			String result = "";
			fullap = vlezni[0];
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
						"http://drakuwa.admin.mk/test.php");

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("command", "add"));
				nameValuePairs.add(new BasicNameValuePair("content", fullap));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse response = httpclient.execute(httppost);

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(
								response.getEntity().getContent(), "UTF-8"));

				result = reader.readLine();
				Log.d("xxx", "First row: " + result);

			} catch (Exception e) {
				// handle exception here
				e.printStackTrace();
			}
			return result;
		}

		/**
		 * What to do after the calculations are finished.
		 */
		public void onPostExecute(String result) {
			// Remove the progress dialog.
			dialog.dismiss();
			if (result.equalsIgnoreCase("Success!")) {
				try {
					File versionfile = new File(path, "version");
					FileWriter fWriter = new FileWriter(versionfile);
					int v = Integer.parseInt(version);
					v++;
					Log
							.d(
									"xxx",
									v
											+ " ova e novata verzija koja shto ke se zapishe...");
					fWriter.write(v + "");
					fWriter.flush();
					fWriter.close();
					version = v + "";

					File collection = new File(path, "collection");
					FileWriter fWriter2 = new FileWriter(collection, true);
					fWriter2.write("\n" + fullap);
					fWriter2.flush();
					fWriter2.close();

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				Toast.makeText(getApplicationContext(),
						"AP successfuly updaed!", Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(getApplicationContext(),
						"Connection error, try again!", Toast.LENGTH_SHORT)
						.show();
		}
	}

	public void addAP() {
		final Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.add_dialog);
		dialog.setTitle("Add AP!");

		WifiInfo w = mWiFiManager.getConnectionInfo();
		final EditText apn = (EditText) dialog.findViewById(R.id.apn);
		apn.setText(w.getSSID());

		final EditText password = (EditText) dialog.findViewById(R.id.password);
		final EditText location = (EditText) dialog.findViewById(R.id.location);
		location.setText(w.getBSSID());

		Button save = (Button) dialog.findViewById(R.id.save);
		Button cancel = (Button) dialog.findViewById(R.id.cancel);

		save.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String ap = apn.getText().toString();
				String pass = password.getText().toString();
				String loc = location.getText().toString();
				if (ap.length() > 0 && pass.length() > 0 && loc.length() > 0) {
					new add().execute(ap + ";" + pass + "*" + loc);
					dialog.dismiss();
				} else
					Toast.makeText(getApplicationContext(),
							"Please enter AP name, Password and BSSID!",
							Toast.LENGTH_SHORT).show();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	public void connectTo(String AP) {
		boolean exists = false;
		// TODO
		// proveri dali e vec konektiran wifi, ako e, nishto rabota. proveri i
		// dali e prisutna konfiguracija i slicno...
		String bssid = AP.substring(AP.indexOf("BSSID: ") + 7, AP
				.indexOf("\nPassword"));
		String psk = AP.substring(AP.indexOf("Password: ") + 10, AP.length());
		String ssid = AP.substring(AP.indexOf("AP name: ") + 9, AP
				.indexOf("\nBSSID"));

		// List available networks
		List<WifiConfiguration> configs = mWiFiManager.getConfiguredNetworks();
		for (WifiConfiguration config : configs) {
			
			Log.d("xxx", config.SSID+" ?= "+ssid);
			if (config.SSID.equalsIgnoreCase("\"" + ssid + "\"")) {
				exists = true;
			}
		}
		Log.d("xxx", "bssid: " + bssid + " psk: " + psk + "*");

		if (!exists) {
			WifiConfiguration wifiConfig = new WifiConfiguration();
			wifiConfig.SSID = "\"" + ssid + "\"";
			wifiConfig.BSSID = bssid;
			wifiConfig.preSharedKey = "\"" + psk + "\"";
			mWiFiManager.setWifiEnabled(true);
			int netId = mWiFiManager.addNetwork(wifiConfig);
			mWiFiManager.enableNetwork(netId, true);
		} else
			Toast.makeText(getApplicationContext(),
					"Network is already configured!", Toast.LENGTH_SHORT)
					.show();
	}
}