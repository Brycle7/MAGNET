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
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class IntentionProc extends Activity{




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
	public static final String  SERIAL			= 	android.os.Build.SERIAL;
	public static final String  TAGS            =   android.os.Build.TAGS;                  //Comma-separated tags describing the build, like "unsigned,debug".
	public static final long    TIME            =   android.os.Build.TIME;
	public static final String  TYPE            =   android.os.Build.TYPE;                  //The type of build, like "user" or "eng".
	public static final String  USER            =   android.os.Build.USER;




	//private WifiP2pManager wifip2p;
	private boolean isWifiP2pEnabled = true;

	public double Intention;
	// private final IntentFilter intentFilter = new IntentFilter();
	//private Channel channel;



	//	public IntentionProc(){
	//		isWifiP2pEnabled = false;
	//		Intention = 0;
	//		status=10;
	//	}

	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}

	private String message = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		TextView textView = new TextView(this);
		textView.setTextSize(20);
		message = String.valueOf(getIntention());
		textView.setText(message);
		setContentView(textView);

		getActionBar().setDisplayHomeAsUpEnabled(true);



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.intention_proc, menu);
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



	/*//getting device name 
    public String getPhoneName(){  
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName();     
        return deviceName;
    }

/////////////////////////////////////  Battery Status ////////////////////////////////////////////////////// 
    public double batteryStatus(){
    	//IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = new Intent(Intent.ACTION_BATTERY_CHANGED); // this.registerReceiver(null, ifilter);

        int currentLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (currentLevel >= 0 && scale > 0) {
            status = (currentLevel * 100) / scale;
        }
		//System.out.println(status);
		return (status*0.03);
    }
/////////////////////////////////////  Battery Status ////////////////////////////////////////////////////// 

/////////////////////////////////////  WiFi P2P Status /////////////////////////////////////////////////////      
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public boolean wifiP2PStatus (){
    	  //intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
          //intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
          //intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
          //intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

          //wifip2p = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

         // channel = wifip2p.initialize(this, getMainLooper(), null);
          //System.out.println("WiFi P2P is: " + isWifiP2pEnabled);
          if (isWifiP2pEnabled){
        	  return false; //- right now it is not working correclt - investigate more
          }
          else{
        	  return true;
          }
    }
/////////////////////////////////////  WiFi P2P Status ///////////////////////////////////////////////////// 

/////////////////////////////////////  WiFi Status /////////////////////////////////////////////////////////    
    public double wifiStatus(Context ctx){
    	int wificonnected=0;
    	double internet=0;


    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    	WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

         double linkSpeed = (wifiManager.getConnectionInfo().getLinkSpeed())*0.01; // scale Link speed to 0 to 1.5 - minimum 0 maximum 150 Mbps
         //int rssi = wifiManager.getConnectionInfo().getRssi();

         NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
                 .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

         ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

       //mobile
       State mobile = conMan.getNetworkInfo(0).getState();

       //WiFi
       State wifi = conMan.getNetworkInfo(1).getState();

         if (info == null || !info.isConnected() || info.isRoaming()) {
        	 internet =  0;
         }
         else {
        	 if((mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) && (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)){
        		 internet = 2; // internet is connected via WiFi and 3G
        	 }
        	 else if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
         		internet =  0.5; // internet is connected over 3G
         	} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
         		internet =  1.5; // internet is connected over WiFi
         	}
         }

    	if (mWifi.isAvailable()) {
    	    if (mWifi.isConnected()){
    	    	wificonnected = 1;
    	    }
    	    else {
    	    	wificonnected = 0;
    	    }
    	}
    	return  (wificonnected + linkSpeed + internet);//statusReturn + "RSSI: " + Integer.toString(rssi) + "dBm/ Link speed: " + Integer.toString(linkSpeed) + 
    			//"\nDistance from AP(GO): " + String.valueOf(calcDistance()) + " meters";
    }
/////////////////////////////////////  WiFi Status /////////////////////////////////////////////////////////  


/////////////////////////////////////  Bluetooth Status ////////////////////////////////////////////////////    
    public int bluetoothStatus(){

    	int bluestatus = 0;
    	int BLEestatus = 0;
    	BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();

    	if (myDevice.isEnabled()){
    		bluestatus = 1;  // Bluetooth enalbled
    	}
    	else {
    		bluestatus = 0;  // Bluetooth not enabled or not available
    	}

    	            // Use this check to determine whether BLE is supported on the device.  Then you can
    	            // selectively disable BLE-related features.
    	            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
    	            	BLEestatus = 0; // BLe not Supported
    	            }
    	            else{
    	            	BLEestatus = 1; //BLE supported
    	            }


    	return (bluestatus + BLEestatus);
    }
/////////////////////////////////////  Bluetooth Status /////////////////////////////////////////////////// 

/////////////////////////////////////  NFC Status //////////////////////////////////////////////////////// 
    public double NFCStatus (){
    	double statusReturn = 0;
    	NfcManager NfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
    	NfcAdapter NfcAdapter = NfcManager.getDefaultAdapter();
    	if (NfcAdapter != null && NfcAdapter.isEnabled()) {
    		statusReturn =  0.5; //"NFC exists and is enabled";
    	}
    	else if (NfcAdapter != null && !NfcAdapter.isEnabled()) {
    		statusReturn =  0.5; //"NFC exists and is disabled";
    	}
    	else {
    		statusReturn =  0; //"NFC does not exist";
    	}
    	return statusReturn;
    }
/////////////////////////////////////  NFC Status ///////////////////////////////////////////////////// 

///////////////////////////////////////  Distance //////////////////////////////////////////////////////
//    //Calculating distance based on RSSI
//    public int calcDistance() {
//
//    	WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
//        int rssi = 0;
//        double distance = 0;
//        double avrage = 0;
//        for (int i =0; i<100; i++){
//        	rssi = wifiManager.getConnectionInfo().getRssi();
//        	distance = 1278.89666284 + 98.19763231 * rssi + 2.69949458* Math.pow(rssi,2)
//        	          + 0.03184348*Math.pow(rssi, 3) + 0.00013895 * Math.pow(rssi,4);
//        	avrage+=(distance/3.2808);
//        }      
//        return (int)(avrage/100);
//}
///////////////////////////////////////  Distance //////////////////////////////////////////////////////

/////////////////////////////////////  CPU Info //////////////////////////////////////////////////////
    private double ReadCPUinfo()
    {
     ProcessBuilder cmd;
     String result="";
     double cpuMaxFreq = 0;

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
      cpuMaxFreq = Double.parseDouble(reader.readLine());
      reader.close();

     } catch(IOException ex){
      ex.printStackTrace();
     }

     return cpuMaxFreq*Math.pow(10, -6);
    }
/////////////////////////////////////  CPU Info //////////////////////////////////////////////////////

/////////////////////////////////////  Mem Info //////////////////////////////////////////////////////
    public double get_Memory(){

    	double max = Runtime.getRuntime().maxMemory(); //the maximum memory the app can use
    	double heapSize = Runtime.getRuntime().totalMemory(); //current heap size
    	double heapRemaining = Runtime.getRuntime().freeMemory(); //amount available in heap
    	double nativeUsage = Debug.getNativeHeapAllocatedSize(); //is this right? I only want to account for native memory that my app is being "charged" for.  Is this the proper way to account for that?

    	//heapSize - heapRemaining = heapUsed + nativeUsage = totalUsage
    	double remaining = max - (heapSize - heapRemaining + nativeUsage); 
    	return (remaining/1048576000);
    }
/////////////////////////////////////  Mem Info //////////////////////////////////////////////////////

    private double is4gavailable() {
    ConnectivityManager connec = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    //NetworkInfo mobileInfo = connec.getNetworkInfo(0);
    //NetworkInfo wifiInfo = connec.getNetworkInfo(1);
    NetworkInfo wimaxInfo = connec.getNetworkInfo(6);


    if (wimaxInfo!=null) {
    	return 0.5; //"Wimax: Supported " + (mobileInfo.isConnectedOrConnecting() || wifiInfo.isConnectedOrConnecting() || wimaxInfo.isConnectedOrConnecting());
    }
    else {
    	return 0;//"WiMax: Not Supported " + (mobileInfo.isConnectedOrConnecting() || wifiInfo.isConnectedOrConnecting());
    }

}

    public double get(){
    	double wifiscaled = 0;
    	if (wifiP2PStatus()){
    		wifiscaled =   	batteryStatus()+ 		// 0 to 3		based on 0 < battery < 100 -- 
    						wifiStatus(this) + 		// 0 to 4.5 	based on WiFi - 3G - Internet - WiFi link speed
    						bluetoothStatus()+ 		// 0 to 2 		based on Bluetooth and BLE
    						NFCStatus() + 			// 0 or 0.5 	based on NFC availability
    						ReadCPUinfo()+ 			// 0 to 3 		based on CPU speed
    						get_Memory() + 			// 0 to 1 		based on remaining memory
    						is4gavailable();		// 0 or 0.5		based on 4G

    		Log.d("Intention2", String.valueOf(batteryStatus()+ " " + wifiStatus(this)+ " "  + bluetoothStatus()+ " " + NFCStatus()+ " "  + ReadCPUinfo()+ " " + get_Memory()+ " "  + is4gavailable()));
    		if (wifiscaled > 14){
    			Intention =  14;
    		}
    		else{
    			Intention =  wifiscaled;
    		}
    	}
    	else{
    		Intention =  0;
    	}
    	return Intention;
    	//System.out.println("Intention of "+ getPhoneName() + " is: " + Intention);
    }

	 */
	private double status;

	//getting device name 
	public String getPhoneName(){  
		BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
		String deviceName = myDevice.getName();     
		return deviceName;
	}

	public String getBasicInfo(){
		return "Android: " + ANDROID;
	}
	/////////////////////////////////////  Battery Status ////////////////////////////////////////////////////// 
	public double batteryStatus(){
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);

		int currentLevel = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		if (currentLevel >= 0 && scale > 0) {
			status = (currentLevel * 100) / scale;
		}
		return status*0.03;
	}

	/////////////////////////////////////  WiFi P2P Status /////////////////////////////////////////////////////      

	public boolean wifiP2PStatus (){

		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		//intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		//wifip2p = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

		//channel = wifip2p.initialize(this, getMainLooper(), null);
		//System.out.println("WiFi P2P is: " + isWifiP2pEnabled);

		if(isWifiP2pEnabled){
			return true;
		}
		else {
			return false;
		}



	}
	/////////////////////////////////////  WiFi P2P Status ///////////////////////////////////////////////////// 

	/////////////////////////////////////  WiFi Status /////////////////////////////////////////////////////////    
	public double wifiStatus(Context ctx){
		int wificonnected=0;
		double internet=0;


		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

		double linkSpeed = (wifiManager.getConnectionInfo().getLinkSpeed())*0.01; // scale Link speed to 0 to 1.5 - minimum 0 maximum 150 Mbps
		//int rssi = wifiManager.getConnectionInfo().getRssi();

		NetworkInfo info = (NetworkInfo) ((ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		//mobile
		State mobile = conMan.getNetworkInfo(0).getState();

		//WiFi
		State wifi = conMan.getNetworkInfo(1).getState();

		if (info == null || !info.isConnected() || info.isRoaming()) {
			internet =  0;
		}
		else {
			if((mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) && (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING)){
				internet = 2; // internet is connected via WiFi and 3G
			}
			else if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
				internet =  0.5; // internet is connected over 3G
			} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
				internet =  1.5; // internet is connected over WiFi
			}
		}

		if (mWifi.isAvailable()) {
			if (mWifi.isConnected()){
				wificonnected = 1;
			}
			else {
				wificonnected = 0;
			}
		}
		return  (wificonnected + linkSpeed + internet);//statusReturn + "RSSI: " + Integer.toString(rssi) + "dBm/ Link speed: " + Integer.toString(linkSpeed) + 
		//"\nDistance from AP(GO): " + String.valueOf(calcDistance()) + " meters";
	}
	/////////////////////////////////////  WiFi Status /////////////////////////////////////////////////////////  



	/////////////////////////////////////  Bluetooth Status ////////////////////////////////////////////////////    
	public int bluetoothStatus(){

		int bluestatus = 0;
		int BLEestatus = 0;
		BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();

		if (myDevice.isEnabled()){
			bluestatus = 1;  // Bluetooth enalbled
		}
		else {
			bluestatus = 0;  // Bluetooth not enabled or not available
		}

		// Use this check to determine whether BLE is supported on the device.  Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			BLEestatus = 0; // BLe not Supported
		}
		else{
			BLEestatus = 1; //BLE supported
		}


		return (bluestatus + BLEestatus);
	}
	/////////////////////////////////////  Bluetooth Status /////////////////////////////////////////////////// 

	/////////////////////////////////////  NFC Status //////////////////////////////////////////////////////// 
	public double NFCStatus (){
		double statusReturn = 0;
		NfcManager NfcManager = (NfcManager) getSystemService(Context.NFC_SERVICE);
		NfcAdapter NfcAdapter = NfcManager.getDefaultAdapter();
		if (NfcAdapter != null && NfcAdapter.isEnabled()) {
			statusReturn =  0.5; //"NFC exists and is enabled";
		}
		else if (NfcAdapter != null && !NfcAdapter.isEnabled()) {
			statusReturn =  0.5; //"NFC exists and is disabled";
		}
		else {
			statusReturn =  0; //"NFC does not exist";
		}
		return statusReturn;
	}
	/////////////////////////////////////  NFC Status ///////////////////////////////////////////////////// 

	/////////////////////////////////////  Distance //////////////////////////////////////////////////////
	/*   //Calculating distance based on RSSI
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
}*/
	/////////////////////////////////////  Distance //////////////////////////////////////////////////////

	/////////////////////////////////////  CPU Info //////////////////////////////////////////////////////
	private double ReadCPUinfo()
	{
		ProcessBuilder cmd;
		String result="";
		double cpuMaxFreq = 0;

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
			cpuMaxFreq = Double.parseDouble(reader.readLine());
			reader.close();

		} catch(IOException ex){
			ex.printStackTrace();
		}

		return cpuMaxFreq*Math.pow(10, -6);
	}
	/////////////////////////////////////  CPU Info //////////////////////////////////////////////////////

	/////////////////////////////////////  Mem Info //////////////////////////////////////////////////////
	public double get_Memory(){

		double max = Runtime.getRuntime().maxMemory(); //the maximum memory the app can use
		double heapSize = Runtime.getRuntime().totalMemory(); //current heap size
		double heapRemaining = Runtime.getRuntime().freeMemory(); //amount available in heap
		double nativeUsage = Debug.getNativeHeapAllocatedSize(); //is this right? I only want to account for native memory that my app is being "charged" for.  Is this the proper way to account for that?

		//heapSize - heapRemaining = heapUsed + nativeUsage = totalUsage
		double remaining = max - (heapSize - heapRemaining + nativeUsage); 
		return (remaining/1048576000);
	}
	/////////////////////////////////////  Mem Info //////////////////////////////////////////////////////

	private double is4gavailable() {
		ConnectivityManager connec = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		//NetworkInfo mobileInfo = connec.getNetworkInfo(0);
		//NetworkInfo wifiInfo = connec.getNetworkInfo(1);
		NetworkInfo wimaxInfo = connec.getNetworkInfo(6);


		if (wimaxInfo!=null) {
			return 0.5; //"Wimax: Supported " + (mobileInfo.isConnectedOrConnecting() || wifiInfo.isConnectedOrConnecting() || wimaxInfo.isConnectedOrConnecting());
		}
		else {
			return 0;//"WiMax: Not Supported " + (mobileInfo.isConnectedOrConnecting() || wifiInfo.isConnectedOrConnecting());
		}

	}

	public double getIntention(){
		double Intention;
		double wifiscaled = 0;
		if (wifiP2PStatus()){
			wifiscaled =   	batteryStatus()+ 		// 0 to 3		based on 0 < battery < 100 -- 
					wifiStatus(this) + 		// 0 to 4.5 	based on WiFi - 3G - Internet - WiFi link speed
					bluetoothStatus()+ 		// 0 to 2 		based on Bluetooth and BLE
					NFCStatus() + 			// 0 or 0.5 	based on NFC availability
					ReadCPUinfo()+ 			// 0 to 3 		based on CPU speed
					get_Memory() + 			// 0 to 1 		based on remaining memory
					is4gavailable();		// 0 or 0.5		based on 4G


			if (wifiscaled > 14){
				Intention =  14;
			}
			else{
				Intention =  wifiscaled;
			}
		}
		else{
			Intention =  0;
		}
		return Intention;
	}

}
