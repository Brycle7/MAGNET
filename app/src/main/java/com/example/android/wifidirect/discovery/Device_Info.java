package com.example.android.wifidirect.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Device_Info extends Activity {

	public static final String  ANDROID         =   android.os.Build.VERSION.RELEASE;       //The current development codename, or the string "REL" if this is a release build.
	public static final String  BOARD           =   android.os.Build.BOARD;                 //The name of the underlying board, like "goldfish".    
	public static final String  BOOTLOADER      =   android.os.Build.BOOTLOADER;            //  The system bootloader version number.
	public static final String  BRAND           =   android.os.Build.BRAND;                 //The brand (e.g., carrier) the software is customized for, if any.
	public static final String  CPU_ABI         =   android.os.Build.CPU_ABI;               //The name of the instruction set (CPU type + ABI convention) of native code.
	public static final String  CPU_ABI2        =   android.os.Build.CPU_ABI2;              //  The name of the second instruction set (CPU type + ABI convention) of native code.
	public static final String  DEVICE          =   android.os.Build.DEVICE;                //  The name of the industrial design.
	public static final String  DISPLAY         =   android.os.Build.DISPLAY;               //A build ID string meant for displaying to the user
	public static final String  FINGERPRINT     =   android.os.Build.FINGERPRINT;           //A string that uniquely identifies this build.
	public static final String  HARDWARE        =   android.os.Build.HARDWARE;              //The name of the hardware (from the kernel command line or /proc).
	public static final String  HOST            =   android.os.Build.HOST;  
	public static final String  ID              =   android.os.Build.ID;                    //Either a changelist number, or a label like "M4-rc20".
	public static final String  MANUFACTURER    =   android.os.Build.MANUFACTURER;          //The manufacturer of the product/hardware.
	public static final String  MODEL           =   android.os.Build.MODEL;                 //The end-user-visible name for the end product.
	public static final String  PRODUCT         =   android.os.Build.PRODUCT;               //The name of the overall product.
	//public static final String  RADIO           =   android.os.Build.RADIO;                 //The radio firmware version number.
	public static final String  SERIAL			= 	android.os.Build.SERIAL;
	public static final String  TAGS            =   android.os.Build.TAGS;                  //Comma-separated tags describing the build, like "unsigned,debug".
	public static final long    TIME            =   android.os.Build.TIME;
	public static final String  TYPE            =   android.os.Build.TYPE;                  //The type of build, like "user" or "eng".
	public static final String  USER            =   android.os.Build.USER;


	//private WifiP2pManager wifip2p;
	//private boolean isWifiP2pEnabled = false;


	//private final IntentFilter intentFilter = new IntentFilter();
	// private Channel channel;


	private int status=10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Intent intent_cal = new Intent(this, IntentionProc.class);
		//startActivity(intent_cal);

		String message = getPhoneName() + "\nCPU Max Freq: " + ReadCPUinfo() + " MHz\nBattery Level: %" + batteryStatus() + "\n" + wifiP2PStatus() + "\n" + wifiStatus() +
				"\n" + haveInternet(this) + "\n" + bluetoothStatus() + "\n" + NFCStatus() + "\nremaining Memory: " + get_Memory() + "MB" + 
				"\n" + is4gavailable() + "\nDevice Macc Address: " + "\nWiFi IP Address: " + getWiFiIPadress() +  devMacaddress() + "\nIPV4: " + Utils.getIPAddress(true) + "\nIPV6: " + Utils.getIPAddress(false) + "\nutil MAC: " + Utils.getMACAddress("wlan0") + "\n" + Utils.getMACAddress("p2p0");
		TextView textView = new TextView(this);
		textView.setTextSize(20);
		textView.setText(message);
		setContentView(textView);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public String getWiFiIPadress(){
		WifiManager mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
		int i = 0;
		if(mWifiInfo!=null) {
			i = mWifiInfo.getIpAddress();
		}
		return ((i & 0xFF) + "." + ((i >> 8) & 0xFF) +
				"." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//getting device name 
	public String getPhoneName(){  
		BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
		String deviceName = myDevice.getName();     
		return "Phone Name: " + deviceName;
	}

	public String getBasicInfo(){
		return "Android: " + ANDROID;
	}
	/////////////////////////////////////  Battery Status ////////////////////////////////////////////////////// 
	public String batteryStatus(){
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);

		int currentLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		if (currentLevel >= 0 && scale > 0) {
			status = (currentLevel * 100) / scale;
		}
		System.out.println(status);
		return Integer.toString(status);
	}
	/////////////////////////////////////  Battery Status ////////////////////////////////////////////////////// 
	private boolean isWifiP2pEnabled = true;
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	/////////////////////////////////////  WiFi P2P Status /////////////////////////////////////////////////////      
	public String wifiP2PStatus (){
		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		//wifip2p = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

		//channel = wifip2p.initialize(this, getMainLooper(), null);
		//System.out.println("WiFi P2P is: " + isWifiP2pEnabled);
		if (isWifiP2pEnabled){
			return "WiFi P2P is enabaled"; //- right now it is not working correclt - investigate more
		}
		else{
			return "WiFi P2P is enabled";
		}
	}
	/////////////////////////////////////  WiFi P2P Status ///////////////////////////////////////////////////// 

	/////////////////////////////////////  WiFi Status /////////////////////////////////////////////////////////    
	public String wifiStatus(){
		String statusReturn = "WiFi Unknow";
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

		int linkSpeed = wifiManager.getConnectionInfo().getLinkSpeed();
		int rssi = wifiManager.getConnectionInfo().getRssi();


		if (mWifi.isAvailable()) {
			if (mWifi.isConnected()){
				statusReturn = "WiFi is Connected to: " + mWifi.getExtraInfo() + " " + mWifi.getSubtypeName();
			}
			else {
				statusReturn = "WiFi is not Connected!";
			}
		}
		else {
			statusReturn = "WiFi is not Available!";
		}
		return statusReturn + "RSSI: " + Integer.toString(rssi) + "dBm/ Link speed: " + Integer.toString(linkSpeed) + 
				"\nDistance from AP(GO): " + String.valueOf(calcDistance()) + " meters";
	}
	/////////////////////////////////////  WiFi Status /////////////////////////////////////////////////////////  

	/////////////////////////////////////  Internet Status /////////////////////////////////////////////////////  
	public String haveInternet(Context ctx) {

		String statusReturn = "Internet Unknow";
		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		//mobile
		State mobile = conMan.getNetworkInfo(0).getState();

		//WiFi
		State wifi = conMan.getNetworkInfo(1).getState();

		if (info == null || !info.isConnected() || info.isRoaming()) {
			statusReturn =  "No Internet available!";
		}
		else {
			if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
				statusReturn =  "Internet Connected via 3G!";
			} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
				statusReturn =  "Internet Connected via WiFi!";
			}
		}
		return statusReturn;       
	}
	/////////////////////////////////////  Internet Status /////////////////////////////////////////////////////  

	/////////////////////////////////////  Bluetooth Status ////////////////////////////////////////////////////    
	public String bluetoothStatus(){

		String statusReturn = "Internet Unknow";
		BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();

		if (myDevice.isEnabled()){
			statusReturn = "Bleutooth is enable";
		}
		else {
			statusReturn = "Bluetooth is disable";
		}
		String statusStr = "status";
		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			statusStr = "BLE: Not Supported";
		}
		else{
			statusStr = "BLE: Supported";
		}


		return statusReturn + "\n" + statusStr;
	}
	/////////////////////////////////////  Bluetooth Status /////////////////////////////////////////////////// 

	/////////////////////////////////////  NFC Status //////////////////////////////////////////////////////// 
	public String NFCStatus (){
		String statusReturn = "NFC Unknow";
		NfcManager NfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
		NfcAdapter NfcAdapter = NfcManager.getDefaultAdapter();
		if (NfcAdapter != null && NfcAdapter.isEnabled()) {
			statusReturn =  "NFC exists and is enabled";
		}
		else if (NfcAdapter != null && !NfcAdapter.isEnabled()) {
			statusReturn =  "NFC exists and is disabled";
		}
		else {
			statusReturn =  "NFC does not exist";
		}
		return statusReturn;
	}
	/////////////////////////////////////  NFC Status ///////////////////////////////////////////////////// 

	/////////////////////////////////////  Distance //////////////////////////////////////////////////////
	//Calculating distance based on RSSI
	public int calcDistance() {

		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		int rssi = 0;
		double distance = 0;
		double avrage = 0;
		for (int i =0; i<100; i++){
			rssi = wifiManager.getConnectionInfo().getRssi();
			distance = 1278.89666284 + 98.19763231 * rssi + 2.69949458* Math.pow(rssi,2)
					+ 0.03184348*Math.pow(rssi, 3) + 0.00013895 * Math.pow(rssi,4);
			avrage+=(distance/3.2808);
		}      
		return (int)(avrage/100);
	}
	/////////////////////////////////////  Distance //////////////////////////////////////////////////////

	/////////////////////////////////////  CPU Info //////////////////////////////////////////////////////
	private String ReadCPUinfo()
	{
		ProcessBuilder cmd;
		String result="";
		String cpuMaxFreq = "";

		try{
			String[] args = {"/system/bin/cat", "/proc/cpuinfo"};
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			InputStream in = process.getInputStream();
			byte[] re = new byte[1024];
			while(in.read(re) != -1){
				System.out.println(new String(re));
				result = result + new String(re); // this result will return all CPU information including number of cores and MIPs and etc. for now we only conside returning CPU max freq
			}
			in.close();

			RandomAccessFile reader = new RandomAccessFile("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq", "r");
			cpuMaxFreq = reader.readLine();
			reader.close();

		} catch(IOException ex){
			ex.printStackTrace();
		}

		return cpuMaxFreq;
	}
	/////////////////////////////////////  CPU Info //////////////////////////////////////////////////////

	/////////////////////////////////////  Mem Info //////////////////////////////////////////////////////
	public int get_Memory(){

		double max = Runtime.getRuntime().maxMemory(); //the maximum memory the app can use
		double heapSize = Runtime.getRuntime().totalMemory(); //current heap size
		double heapRemaining = Runtime.getRuntime().freeMemory(); //amount available in heap
		double nativeUsage = Debug.getNativeHeapAllocatedSize(); //is this right? I only want to account for native memory that my app is being "charged" for.  Is this the proper way to account for that?

		//heapSize - heapRemaining = heapUsed + nativeUsage = totalUsage
		double remaining = max - (heapSize - heapRemaining + nativeUsage); 
		return (int)(remaining/1048576);
	}
	/////////////////////////////////////  Mem Info //////////////////////////////////////////////////////

	private String is4gavailable() {
		ConnectivityManager connec = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileInfo = connec.getNetworkInfo(0);
		NetworkInfo wifiInfo = connec.getNetworkInfo(1);
		NetworkInfo wimaxInfo = connec.getNetworkInfo(6);


		if (wimaxInfo!=null) {
			return "Wimax: Supported " + (mobileInfo.isConnectedOrConnecting() || wifiInfo.isConnectedOrConnecting() || wimaxInfo.isConnectedOrConnecting());
		}
		else {
			return "WiMax: Not Supported " + (mobileInfo.isConnectedOrConnecting() || wifiInfo.isConnectedOrConnecting());
		}

	}

	private String devMacaddress(){

		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo = wifiManager.getConnectionInfo();
		String macAddress = wInfo.getMacAddress(); 
		return macAddress;


	}

}

