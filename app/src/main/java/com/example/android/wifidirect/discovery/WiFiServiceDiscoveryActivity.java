package com.example.android.wifidirect.discovery;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Layout;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifidirect.discovery.WiFiChatFragment.MessageTarget;
import com.example.android.wifidirect.discovery.WiFiDirectServicesList.DeviceClickListener;
import com.example.android.wifidirect.discovery.WiFiDirectServicesList.WiFiDevicesAdapter;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

public class WiFiServiceDiscoveryActivity extends Activity implements DeviceClickListener, Handler.Callback, MessageTarget,
        ConnectionInfoListener, DnsSdServiceResponseListener, DnsSdTxtRecordListener, PeerListListener, ChannelListener {

    // Constant Variables
    public static final String SERVICE_REG_TYPE = "_presence._tcp"; // this is registeration type for services
    public static final String TAG = "wifidirectdemo";              // A common tag for LogCat

    public static final int MESSAGE_READ = 0x400 + 1;               // Reserved Code for Message Handler
    public static final int MY_HANDLE_P2P = 0x400 + 2;              // Reserved Code for Message Handler
    public static final int OBJECT_READ = 0x400 + 3;                // Reserved Code for Message Handler
    public static final int ALCON_APLIST_RESPONSE = 0x400 + 4;      // Reserved Code for Message Handler
    public static final int ALCON_APLIST_REQUEST = 0x400 + 5;       // Reserved Code for Message Handler
    public static final int ALCON_AP_CONNECTION = 0x400 + 6;        // Reserved Code for Message Handler
    public static final int DELIVER_MESSAGE = 0x400 + 7;            // Reserved Code for Message Handler
    public static final int TEST_GROUP_CONNECTION = 0x400 + 8;      // Reserved Code for Message Handler

    public static final int waitcoeff = 600;                        // Waiting time will be multiply by this coefficient (milliseconds)
    //public static final int SERVER_PORT = 4545;                     // Server port is the same in GroupOwnerSocketHandler
    //Device Status
    public static final int CONNECTED = 0;
    public static final int INVITED = 1;
    public static final int FAILED = 2;
    public static final int AVAILABLE = 3;
    public static final int UNAVAILABLE = 4;

    // None constant variables
    public boolean isWifiP2pEnabled = false;                        // would be set to true by Broadcast receiver when the wifi P2P enabled
    public boolean isGroupOwner = false;                            // would be set to true by ConnectionInfoListener when connection info avilable
    public boolean isConnected = false;                             // would be set to true by Broadcast receiver when the device state changed
    private boolean SMARTSelected = false;
    public double intention = 0;                                    // The intention of this device. we may set it once to save memory instead of calling getIntention() method several times
    private boolean appTerminate = false;
    private boolean isGroupFormed = false;
    private boolean apListRequested = false;
    private boolean alreadyConnected = false;
    private static String p2pMacAddress = null;
    private static String wifiMacAddress = null;
    private static InetAddress broadcastAddress;

    // Objetc definitions
    private WifiP2pManager manager;
    private WifiManager wifiManager;
    private ChatManager P2pChatManager = null;
    private DataGramSender dataGramSender = null;
    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private Handler handler;
    private TextView statusTxtView;
    private Handler delayHandler1;                                   // for Handling delays
    private Handler delayHandler2;                                   // needed for nested delays
    private Handler delayHandler3;                                   // needed for nested delays
    private Handler delayHandler4;                                   // needed for nested delays
    private Handler delayHandlerResetDis;                            // Delay handler for restating discovery
    private Handler delayHandlerPPresolve;                           // Delay handler for resolving SSID and passPhrase
    private Handler delayHandlerZeroClient;                          // Delay handler for resolving Zero Client Groups
    private Handler delayHandlerDecisionMaking;                      // Delay Hndlert for Decision Making
    private Handler delayHandlerRclientAPList;
    private Handler delayHandlerTestMessage;
    private WifiP2pConfig config;                                    // configuration of wifi p2p. here for WPS configuration

    // Collections
    private List<WiFiP2pService> listFragmentService;
    private List<WifiP2pDevice> proximityGroupList;                  // List of Proximal Groups
    private ArrayList<WifiP2pDevice> peerList;                       // WifiP2pDevice of peers found in the proximity
    private ArrayList<WifiP2pDevice> groupedPeerList;                // WifiP2pDevice of peers in the group
    private ArrayList<ChatManager> chatClientList;                   // List of ChatManagers. A chatManager Object is needed for each client to send message
    private List<WTAClass> WTAList;
    public List<String> wifiAPList;
    public List<ScanResultMagnet> directAPList;
    private Random r = new Random();
    public String cGroupSSID = "";                                   // current group SSId if this device is group owner
    private String currentAPSSID = "";                               // current SSID that this client is connected to
    private HashMap<String, String> SSIDPassMap;                     // SSID <=> NetworkPASS fir AP connection
    private List<String> permittedMacAddressList;
    private boolean forcedCreateGroup = false;
    private boolean isWifiConnected = false;
    private boolean threadAlreadyCreated = false;
    public String apListReByMac = "";
    private Thread udpReceiverThread = null;
    private Thread legacyHandler = null;
    private boolean udpReceiverCreated = false;
    private boolean servicePasscreated = false;
    private WifiP2pDnsSdServiceInfo serviceSSIDPass;

    private HashMap<String, ArpDevice> connectedDevicesMap = new HashMap<String, ArpDevice>();
    private static boolean p2pNotDetected = true;
    private List<Long> alreadyForwarded = new ArrayList<Long>();

    private final String testNum = "Test6";
    private final String testExplanation = "A test with better logging with more details";
    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    // Normally everything can be initialized on onCreate method
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        handler = new Handler(this);
        delayHandler1 = new Handler();                                // for Handling delays
        delayHandler2 = new Handler();                                // needed for nested delays
        delayHandler3 = new Handler();                                // needed for nested delays
        delayHandler4 = new Handler();                                // needed for nested delays
        delayHandlerResetDis = new Handler();
        delayHandlerPPresolve = new Handler();
        delayHandlerZeroClient = new Handler();
        delayHandlerDecisionMaking = new Handler();
        delayHandlerRclientAPList = new Handler();
        delayHandlerTestMessage = new Handler();
        config = new WifiP2pConfig();                                 // configuration of wifi p2p. here for WPS configuration
        proximityGroupList = new ArrayList<WifiP2pDevice>();          // Group list when receiving list of proximal devices
        peerList = new ArrayList<WifiP2pDevice>();                    // WifiP2pDevice of peers found in the proximity
        groupedPeerList = new ArrayList<WifiP2pDevice>();             // WifiP2pDevice of peers in the group
        chatClientList = new ArrayList<ChatManager>();                // List of ChatManagers. A chatManager Object is needed for each client to send message
        WTAList = new ArrayList<WTAClass>();
        wifiAPList = new ArrayList<String>();
        directAPList = new ArrayList<ScanResultMagnet>();
        SSIDPassMap = new HashMap<String, String>();
        listFragmentService = new ArrayList<WiFiP2pService>();
        permittedMacAddressList = new ArrayList<String>();

        SSIDPassMap.put("DIRECT-5w-Android_617d", "2Bdy7Szx");        //b6:ce:f6:08:ca:a2
        SSIDPassMap.put("DIRECT-OF-Android_1e9b", "7ycL6HM2");        //b6:ce:f6:09:7c:49
        SSIDPassMap.put("DIRECT-bX-Galaxy S4", "naPzu5qL");           //42:0E:85:4A:3E:0F
        SSIDPassMap.put("DIRECT-0b-Android_4a1d", "tuG3BaAi");        //b6:ce:f6:08:c9:da
        SSIDPassMap.put("DIRECT-DG-Galaxy S6 edge", "SOFZmpaD");      //EA:50:8B:F0:24:AD
        SSIDPassMap.put("DIRECT-U7-Android_640f", "mPo0M6ed");        //52:2e:5c:e5:1a:02
        SSIDPassMap.put("DIRECT-fX-Android_5f6f", "UeWZXmHp");        //ce:3a:61:ba:7f:bb
        SSIDPassMap.put("DIRECT-sK-Android_64e6", "Bu5fm8DF");        //b6:ce:f6:df:39:a7
        SSIDPassMap.put("DIRECT-qW-XT1068_f06b", "wA7TELSo");         //9c:d9:17:6c:e8:84
        SSIDPassMap.put("DIRECT-4w-Galaxy S4 d0tted", "Y0w7bO8F");    //ce:3a:61:b7:51:28
        SSIDPassMap.put("DIRECT-Ny-Galaxy S4", "EgphMFQb");           //42:0e:85:4a:3e:0f

        permittedMacAddressList.add("b6:ce:f6:08:ca:a2");
        permittedMacAddressList.add("b6:ce:f6:09:7c:49");
        permittedMacAddressList.add("42:0E:85:4A:3E:0F");
        permittedMacAddressList.add("b6:ce:f6:08:c9:da");
        permittedMacAddressList.add("EA:50:8B:F0:24:AD");
        permittedMacAddressList.add("52:2e:5c:e5:1a:02");
        permittedMacAddressList.add("ce:3a:61:ba:7f:bb");
        permittedMacAddressList.add("b6:ce:f6:df:39:a7");
        permittedMacAddressList.add("9c:d9:17:6c:e8:84");
        permittedMacAddressList.add("ce:3a:61:b7:51:28");
        permittedMacAddressList.add("42:0e:85:4a:3e:0f");

        statusTxtView = (TextView) findViewById(R.id.status_text);     // Status text at the bottom of main activity
        statusTxtView.setMovementMethod(new ScrollingMovementMethod());// making Status text view scrollable
        statusTxtView.setScrollbarFadingEnabled(false);                // Set the Scroll bar visible all the time

        // Adding necessary change action to the intent filter
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        // Initializing wifiP2P manager, Channel and Broadcast receiver and registering the receiver
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), this);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            wifiManager.setWifiEnabled(true);
        }


        // remove the previous list of configured Direct AP networks
        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration i : list) {
            if (i.SSID != null) {
                if (i.SSID.contains("DIRECT")) {
                    wifiManager.removeNetwork(i.networkId);
                    appendStatus(i.SSID + " removed!");
                }
            }
        }

        // Configure the Intention and WPS in wifi P2P
        config.groupOwnerIntent = 8 + r.nextInt(6);
        appendStatus("GO Intent: " + config.groupOwnerIntent);
        config.wps.setup = WpsInfo.PBC;

        p2pMacAddress = Utils.getMACAddress("p2p0");
        wifiMacAddress = Utils.getMACAddress("wlan0");
        try {
            broadcastAddress = InetAddress.getByName("192.168.49.255");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        File folder = new File(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum);
        if (!folder.exists()) {
            folder.mkdir();
        }

        FileWriter fileLogWriter;
        try {
            fileLogWriter = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/messageReceived" + ".txt", true);
            fileLogWriter.write(testExplanation+  " Mac Address: " + p2pMacAddress   + "Device Model: "+ android.os.Build.MODEL + " API: " + Build.VERSION.RELEASE + "\n");
            fileLogWriter.flush();
            fileLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileLogWriter = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/messageSent" + ".txt", true);
            fileLogWriter.write(testExplanation +" Mac Address: " + p2pMacAddress   +"Device Model: "+ android.os.Build.MODEL + " API: " + Build.VERSION.RELEASE +  "\n");
            fileLogWriter.flush();
            fileLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileLogWriter = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/wifiConnection" + ".txt", true);
            fileLogWriter.write(testExplanation +" Mac Address: " + p2pMacAddress   +"Device Model: "+ android.os.Build.MODEL + " API: " + Build.VERSION.RELEASE +  "\n");
            fileLogWriter.flush();
            fileLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            fileLogWriter = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/p2pConnection" + ".txt", true);
            fileLogWriter.write(testExplanation +" Mac Address: " + p2pMacAddress   + "Device Model: "+ android.os.Build.MODEL + " API: " + Build.VERSION.RELEASE + "\n");
            fileLogWriter.flush();
            fileLogWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        intention = config.groupOwnerIntent + r.nextDouble();           // calculating the intention and adding a small value to prevent intention similarity
        startRegistrationAndDiscovery();                                // Calling the first method in this activity
        WiFiDirectServicesList servicesList = new WiFiDirectServicesList();
        getFragmentManager().beginTransaction().add(R.id.container_root, servicesList, "services").commit();
    }

    //Registers a local service and then initiates a service discovery
    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();
        record.put("Intention", String.valueOf(intention));
        WifiP2pDnsSdServiceInfo serviceMagnet = WifiP2pDnsSdServiceInfo.newInstance(
                "MAGNET", SERVICE_REG_TYPE, record);
        manager.addLocalService(channel, serviceMagnet, new ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Added Local Service: " + "MAGNET");
            }

            @Override
            public void onFailure(int error) {
                appendStatus("Failed to add a service: " + desError(error));
            }
        });
        // Start Service discovery
        discoverService();
    }

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        // A service has been discovered. For each new discovered service this method would called
        // Update the UI in the main activity
        if (instanceName.equals("MAGNET")) {
            boolean deviceAv = false;
            for (WiFiP2pService service : listFragmentService) {
                if (service.device.deviceAddress.equals(srcDevice.deviceAddress)) {
                    service.device = srcDevice;
                    service.instanceName = instanceName;
                    service.serviceRegistrationType = registrationType;
                    deviceAv = true;
                    break;
                }
            }

            if (!deviceAv) {
                WiFiP2pService aNewService = new WiFiP2pService();
                aNewService.device = srcDevice;
                aNewService.instanceName = instanceName;
                aNewService.serviceRegistrationType = registrationType;
                aNewService.setIntention(13);
                listFragmentService.add(aNewService);
            }
        }
        if (!instanceName.equals("MAGNET") && instanceName.contains("SSID")) {
            boolean deviceAv = false;
            for (WiFiP2pService service : listFragmentService) {
                if (service.device.deviceAddress.equals(srcDevice.deviceAddress)) {
                    if (service.PassPhrase != null) {
                        service.device = srcDevice;
                        service.instanceName = instanceName;
                        service.serviceRegistrationType = registrationType;
                        deviceAv = true;
                        break;
                    }
                }
            }

            if (!deviceAv) {
                WiFiP2pService aNewService = new WiFiP2pService();
                aNewService.device = srcDevice;
                aNewService.instanceName = instanceName;
                aNewService.serviceRegistrationType = registrationType;
                aNewService.setIntention(13);
                listFragmentService.add(aNewService);
            }
        }
        updateFragment();
    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice srcDevice) {
        if (record.get("Intention") != null) {
            Log.d("Intentions", srcDevice.deviceName + " " + record.get("Intention"));
            //appendStatus(srcDevice.deviceAddress + " Intention: " + record.get("Intention"));

            boolean deviceAv = false;
            for (WiFiP2pService service : listFragmentService) {
                if (service.device.deviceAddress.equals(srcDevice.deviceAddress)) {
                    service.device = srcDevice;
                    service.setIntention(Double.parseDouble(record.get("Intention")));
                    deviceAv = true;
                    break;
                }
            }

            if (!deviceAv) {
                WiFiP2pService aNewService = new WiFiP2pService();
                aNewService.device = srcDevice;
                aNewService.instanceName = null;
                aNewService.serviceRegistrationType = null;
                aNewService.setIntention(Double.parseDouble(record.get("Intention")));
                listFragmentService.add(aNewService);
            }
        }
        if (record.get("PassPhrase") != null) {
            boolean deviceAv = false;
            for (WiFiP2pService service : listFragmentService) {
                if (service.device.deviceAddress.equals(srcDevice.deviceAddress)) {
                    service.device = srcDevice;
                    service.PassPhrase = record.get("PassPhrase");
                    deviceAv = true;
                    break;
                }
            }

            if (!deviceAv) {
                WiFiP2pService aNewService = new WiFiP2pService();
                aNewService.device = srcDevice;
                aNewService.instanceName = null;
                aNewService.serviceRegistrationType = null;
                aNewService.PassPhrase = record.get("PassPhrase");
                listFragmentService.add(aNewService);
            }
        }
        updateFragment();
    }

    // Start Discovery
    private void discoverService() {

		/*
         * Register listeners for DNS-SD services. These are callbacks invoked
		 * by the system when a service is actually discovered.
		 */

        manager.setDnsSdResponseListeners(channel, this, this);

        // After attaching listeners, create a service request and initiate discovery.
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest, new ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Added service discovery request");
            }

            @Override
            public void onFailure(int arg0) {
                appendStatus("Failed adding service discovery request: " + desError(arg0));
            }
        });
        manager.discoverServices(channel, new ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Service discovery initiated");
            }

            @Override
            public void onFailure(int arg0) {
                appendStatus("Service discovery failed: " + desError(arg0));
                restartServiceDiscovery();
            }
        });

        // delay Handler for service discovery - If the discovery started and the device was not able to find any devices
        // it may be a bug in the discovery so we do the discovery again periodically.
        delayHandlerResetDis.postDelayed(new Runnable() { // wait. if no device found after 20 seconds. Restart service discovery
            public void run() {
                if (listFragmentService.isEmpty()) {
                    appendStatus("Problem in Service Discovery!");
                    appendStatus("Restarting Service Discovery...");
                    restartServiceDiscovery();
                }
                delayHandlerResetDis.postDelayed(this, 60000);
            }
        }, 60000);

        // Resolve Zero client groups
        delayHandlerZeroClient.postDelayed(new Runnable() { // wait. if no device found after 20 seconds. Restart service discovery
            public void run() {
                if (isGroupOwner && groupedPeerList.isEmpty() && !proximityGroupList.isEmpty()) {
                    appendStatus("Resolve Zero Client Groups!");
                    manager.removeGroup(channel, new ActionListener() {
                        @Override
                        public void onSuccess() {
                            config.deviceAddress = proximityGroupList.get(0).deviceAddress;
                            manager.connect(channel, config, new ActionListener() {
                                @Override
                                public void onSuccess() {
                                    appendStatus("GO: connection request sent to: " + config.deviceAddress);
                                }

                                @Override
                                public void onFailure(int i) {
                                    appendStatus("Failed to connect to another group. Restarting service discovery");
                                    restartServiceDiscovery();
                                }
                            });
                        }

                        @Override
                        public void onFailure(int i) {
                            appendStatus("Failed to remove Group. Still the GO");
                        }
                    });

                } else if (isGroupOwner && groupedPeerList.isEmpty() && proximityGroupList.isEmpty()) {
                    restartServiceDiscovery();
                }
                delayHandlerZeroClient.postDelayed(this, 200000);
            }
        }, 200000);

        // DRequest Client AP List every 30 Seconds
        delayHandlerRclientAPList.postDelayed(new Runnable() { // Decide to connect to another groups every 39 seconds
            public void run() {
                if (isGroupOwner && !groupedPeerList.isEmpty()) {
                    requestClientApList();
                    // Decision Making to connect to external groups after 10 seconds of AP List request
                    delayHandlerDecisionMaking.postDelayed(new Runnable() {
                        public void run() {
                            if (isGroupOwner && !WTAList.isEmpty()) {
                                HashMap<String, String> theDecisionResult = decideGroupConnection(WTAList);
                                for (HashMap.Entry<String, String> entry : theDecisionResult.entrySet()) {
                                    String clientMAC = entry.getKey();
                                    String apSSID = entry.getValue();

                                    magnetMessage newMessage = new magnetMessage(ALCON_AP_CONNECTION, apSSID, " ", p2pMacAddress, clientMAC);
                                    if (dataGramSender == null) {
                                        dataGramSender = new DataGramSender(8027, broadcastAddress);
                                        dataGramSender.start();
                                    }

                                    dataGramSender.setMsg(newMessage);
                                    appendStatus("A command to connect to:" + apSSID + " sent to: " + clientMAC);
                                }
                            }
                        }
                    }, 10000);
                }
                delayHandlerRclientAPList.postDelayed(this, 30000);
            }
        }, 30000);

        // Send Test Message every 10 seconds
        delayHandlerTestMessage.postDelayed(new Runnable() { // Decide to connect to another groups every 39 seconds
            public void run() {
                if (isConnected || isWifiConnected) {
                    for (String address : permittedMacAddressList) {
                        sendMessage(Utils.getCurrentTimeStamp(), address);
                    }
                }
                delayHandlerTestMessage.postDelayed(this, 10000);
            }
        }, 10000);
    }

    // This method actually will be called when the related view has been clicked on the fragment.
    // But to make the click autonomous, I have also start this method after couple of seconds by passing "MAGNET" to it
    // which means I mimic the click scenario
    @Override
    public void connectP2p(WiFiP2pService service) {
        //Finding the maximum intention inside the intentionList HashMap
        double ain = 0;
        for (WiFiP2pService wifip2pervice : listFragmentService) {
            if (wifip2pervice.getIntention() > ain) {
                ain = wifip2pervice.getIntention();
            }
        }
        //Check to see whether this device has the highest Intention
        //If this device has the highest intention it will create a group
        //else it will wait for the other device to create a group. if no groups found it will create one

        if (intention > ain) {
            //This device has the highest Intention
            manager.requestPeers(channel, new PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    updatePeerLists(peers);
                    appendStatus("This device has the highest Intention");
                    if (!isConnected && proximityGroupList.isEmpty()) {   // IF this device is not connected and there is not any group around
                        appendStatus("No Other groups; Creating a new group");
                        createGroup();
                    } else if (!isConnected) {

                        //If the device is not connected and there are some groups around
                        //Send request to joing to all available group owners
                        // for (WifiP2pDevice device : proximityGroupList) {
                        connecPeer(proximityGroupList.get(0).deviceAddress);
                        appendStatus("Invitation sent to Group: " + proximityGroupList.get(0).deviceName);
                        // }
                        //wait for couple of seconds and then check to see if it is connected. otherwise create a group
                        appendStatus("waiting for connection: " + calculateWait() + " ms");
                        delayHandler1.postDelayed(new Runnable() {
                            public void run() {
                                if (!isConnected) {
                                    appendStatus("All Requests failed; Creating a group");
                                    createGroup();
                                }
                            }
                        }, calculateWait());
                    }
                }
            });
        } else {
            //Not the highest Intention. It will wait and then check the group list for any
            // available groups and try to connect to the group owner of each group.
            appendStatus("Not the highest Intention");
            appendStatus("will wait for " + calculateWait() + " ms");
            delayHandler1.postDelayed(new Runnable() {
                public void run() {
                    if (!isConnected) {
                        manager.requestPeers(channel, new PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList peers) {
                                updatePeerLists(peers);
                                if (!proximityGroupList.isEmpty() && !isConnected) {                // if there are some groups around send request to join
                                    appendStatus("Invitation sent to Group: " + proximityGroupList.get(0).deviceName);
                                    connecPeer(proximityGroupList.get(0).deviceAddress);
                                    appendStatus("Wait for GO response: " + calculateWait() + " ms");
                                    delayHandler2.postDelayed(new Runnable() { // wait. if not connected after sending request to all groups, create a group
                                        public void run() {
                                            if (!isConnected) {
                                                appendStatus("All Requests failed; Creating a group");
                                                createGroup();
                                            }
                                        }
                                    }, calculateWait());
                                } else if (proximityGroupList.isEmpty() && !isConnected) {
                                    // if there is not any group, restart service discovery and check again to be sure that there is not any groups available
                                    appendStatus("No Other groups; creating group");
                                    createGroup();
                                }
                            }
                        });
                    }
                }
            }, calculateWait());
        }
        SMARTSelected = false;
    }

    // Group Creation
    private void createGroup() {
        if (isConnected && !forcedCreateGroup) {
            return;
        } else if (forcedCreateGroup) {
            forcedCreateGroup = false;
        }
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Group Created Successfully!");
            }

            @Override
            public void onFailure(int reason) {
                appendStatus("Failed to create a group: " + desError(reason));
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        updatePeerLists(peers);
                        if (!isConnected && !SMARTSelected) {
                            SMARTSelected = true;
                            delayHandler4.postDelayed(new Runnable() {
                                public void run() {
                                    WiFiP2pService newService = new WiFiP2pService();
                                    newService.instanceName = "MAGNET";
                                    appendStatus("SMART selected= true");
                                    connectP2p(newService);
                                }
                            }, calculateWait());
                        }
                    }
                });
            }
        });
    }

    // get the device MAC address and connect to the peer
    public void connecPeer(String deviceMacAddress) {
        //  if (permittedMacAddressList.contains(deviceMacAddress)) {
        config.deviceAddress = deviceMacAddress;
        manager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                appendStatus("Connection Request Sent to: " + config.deviceAddress);
            }

            @Override
            public void onFailure(int errorCode) {
                appendStatus("Failed to send connection request:" + config.deviceAddress + desError(errorCode));
            }
        });
        // }
    }

    //when there is a message to read at the socket this method will be called
    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:

                appendStatus("MESSAGE_READ received:" + ((magnetMessage) msg.obj).object);
                break;

            case OBJECT_READ:

                magnetMessage magnetmessage = (magnetMessage) msg.obj;
                appendStatus("A message object received: " + magnetmessage.receiverMAC);

                if (magnetmessage.senderMAC.equals(p2pMacAddress)) {
                    appendStatus("discarding self message");
                    break;
                }

                if (!(magnetmessage.receiverMAC.equals(p2pMacAddress) || magnetmessage.receiverMAC.equals(wifiMacAddress) || magnetmessage.receiverMAC.equals("BROADCAST") || magnetmessage.what == DELIVER_MESSAGE)) {
                    appendStatus("Message destination is not me!");
                    /*if (!isGroupOwner && isConnected && isWifiConnected) {
                        // this is a bridge - forward the message
                        appendStatus("Bridging the message...");
                        magnetMessage forwardMessage = new magnetMessage(magnetmessage.what, magnetmessage.object, magnetmessage.senderIP, magnetmessage.senderMAC, magnetmessage.receiverMAC);
                        if (dataGramSender == null) {
                            dataGramSender = new DataGramSender(5050, broadcastAddress);
                            dataGramSender.start();
                        }
                        dataGramSender.setMsg(forwardMessage);
                        appendStatus("Message Forwarded via UDP");
                        if (P2pChatManager != null) {
                            P2pChatManager.writeObject(forwardMessage);
                            appendStatus("Message Forwarded via TCP");
                        }
                    }*/
                    break;
                }

                // if this is a request from group owner for access point list which could be see by this client
                // Start WiFi Scan and send back the fresh result
                if (magnetmessage.what == ALCON_APLIST_REQUEST) {
                    appendStatus("Updating WiFi APs...");
                    apListReByMac = magnetmessage.senderMAC;
                    apListRequested = true;
                    wifiManager.startScan();
                    // if this is a list of access point from client to the group owner
                } else if (magnetmessage.what == ALCON_APLIST_RESPONSE) {
                    appendStatus("AP List Received");
                    List<ScanResultMagnet> APDList = (List<ScanResultMagnet>) magnetmessage.object;

                    // Remove the current device (which is a GO) from the seen Direct AP list of the clients
                    for (Iterator<ScanResultMagnet> iterator = APDList.iterator(); iterator.hasNext(); ) {
                        ScanResultMagnet SRMagnet = iterator.next();
                        if (SRMagnet.SSID.equals(cGroupSSID)) {
                            // Remove the current element from the iterator and the list.
                            iterator.remove();
                        }
                    }

                    appendStatus("Direct found as: " + " Devices IP: " + magnetmessage.senderIP + "\n" + APDList);
                    WTAClass newWTA = new WTAClass(magnetmessage.senderMAC);
                    newWTA.interfaceIP = magnetmessage.senderIP;
                    newWTA.setGroupSeen(APDList);
                    WTAList.add(newWTA);

                    // if this is a command from group owner to this client to connect to an access point
                } else if (magnetmessage.what == ALCON_AP_CONNECTION) {
                    appendStatus("AP Connection Request to: " + magnetmessage.object);
                    final String networkSSID = (String) magnetmessage.object;

                    // check if this device has not already connected to the requested SSID
                    if (wifiManager != null) {
                        WifiInfo wifiinfo = wifiManager.getConnectionInfo();
                        if (wifiinfo.getSSID().equals(networkSSID)) {
                            appendStatus("Already Connected to: " + networkSSID);
                            break;
                        }
                    }

                    if (networkSSID.contains("Galaxy S6") || networkSSID.contains("Galaxy S4 d0tted")) {
                        appendStatus("PassPhrase need resolve");
                        restartServiceDiscovery();
                        delayHandlerPPresolve.postDelayed(new Runnable() { // wait 10 seconds for discovery to update serviceListFragment
                            public void run() {
                                appendStatus("Trying to get PassPhrase");
                                for (WiFiP2pService service : listFragmentService) {
                                    if (service.instanceName.contains("SSID") && SubStringBetween.subStringBetween(service.instanceName, "SSID:", ":PassPhrase:").equals(networkSSID)) {
                                        String networkPass = service.instanceName.substring(service.instanceName.indexOf("PassPhrase:") + 1, service.instanceName.length());
                                        appendStatus("PassPhrase Found in Service.name: " + networkPass);
                                        WifiConfiguration conf = new WifiConfiguration();
                                        conf.SSID = "\"" + networkSSID + "\"";
                                        conf.preSharedKey = "\"" + networkPass + "\"";
                                        wifiManager.addNetwork(conf);
                                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                                        for (WifiConfiguration i : list) {
                                            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                                                wifiManager.disconnect();
                                                wifiManager.enableNetwork(i.networkId, true);
                                                wifiManager.reconnect();
                                                break;
                                            }
                                        }
                                        break;
                                    } else if (service.PassPhrase != null && SubStringBetween.subStringBetween(service.PassPhrase, "SSID:", ":PassPhrase:").equals(networkSSID)) {
                                        String networkPass = service.PassPhrase.substring(service.PassPhrase.indexOf("PassPhrase:") + 1, service.PassPhrase.length());
                                        appendStatus("PassPhrase Found in Service.PassPhrase: " + networkPass);
                                        WifiConfiguration conf = new WifiConfiguration();
                                        conf.SSID = "\"" + networkSSID + "\"";
                                        conf.preSharedKey = "\"" + networkPass + "\"";
                                        wifiManager.addNetwork(conf);
                                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                                        for (WifiConfiguration i : list) {
                                            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                                                wifiManager.disconnect();
                                                wifiManager.enableNetwork(i.networkId, true);
                                                wifiManager.reconnect();
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }, 10000);
                        break;
                    } else {
                        appendStatus("trying to connect to a known SSID");
                        String networkPass = SSIDPassMap.get(networkSSID);
                        WifiConfiguration conf = new WifiConfiguration();
                        conf.SSID = "\"" + networkSSID + "\"";
                        conf.preSharedKey = "\"" + networkPass + "\"";
                        wifiManager.addNetwork(conf);
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        for (WifiConfiguration i : list) {
                            if (i.SSID != null && i.SSID.equals("\"" + networkSSID + "\"")) {
                                wifiManager.disconnect();
                                wifiManager.enableNetwork(i.networkId, true);
                                wifiManager.reconnect();
                                break;
                            }
                        }
                    }
                } else if (magnetmessage.what == TEST_GROUP_CONNECTION) {
                    appendStatus(magnetmessage.object + " " + magnetmessage.senderMAC);

                } else if (magnetmessage.what == DELIVER_MESSAGE) {
                    if (magnetmessage.receiverMAC.equals(p2pMacAddress) || magnetmessage.receiverMAC.equals(wifiMacAddress)) {
                        appendStatus("A message Received from: " + magnetmessage.receiverMAC + " :: " + magnetmessage.object);
                        // Log the file with the time stamps
                        FileWriter fileLogMessage;
                        try {
                            fileLogMessage = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/messageReceived" + ".txt", true);
                            appendStatus(fileLogMessage.toString());
                            fileLogMessage.write("Sent on: " + magnetmessage.object + " From: " + magnetmessage.senderMAC + "(" + magnetmessage.senderIP + ")" +
                                    " Received on: " + Utils.getCurrentTimeStamp() + " On this Device: " + p2pMacAddress + "("+ magnetmessage.receiverMAC +")" +
                                    " ID: " + magnetmessage.messageID + " Number of hobs: " + magnetmessage.hopNum + " isGroupOwner: " + isGroupOwner + " isWiFiConnected: " + isWifiConnected +
                                    " isConnected: " + isConnected + "\n");
                            fileLogMessage.flush();
                            fileLogMessage.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (!alreadyForwarded.contains(magnetmessage.messageID)) {
                        appendStatus("Forwarding the Message: " + alreadyForwarded.size());
                        alreadyForwarded.add(magnetmessage.messageID);
                        magnetmessage.hopNum++;
                        // forward it to others
                        if (isGroupOwner) {
                            appendStatus("Forward Message as GroupOwner");
                            if (dataGramSender == null) {
                                dataGramSender = new DataGramSender(8027, broadcastAddress);
                                dataGramSender.start();
                            }
                            dataGramSender.setMsg(magnetmessage);

                            if (!chatClientList.isEmpty()) {
                                for (ChatManager chatManager : chatClientList) {
                                    chatManager.writeObject(magnetmessage);
                                }
                            }
                        } else if (isConnected && isWifiConnected) {
                            appendStatus("Forward Message as Bridge");
                            if (dataGramSender == null) {
                                dataGramSender = new DataGramSender(5050, broadcastAddress);
                                dataGramSender.start();
                            }
                            dataGramSender.setMsg(magnetmessage);
                            if (P2pChatManager != null) {
                                P2pChatManager.writeObject(magnetmessage);
                            }
                        }
                    }
                }
                break;

            case MY_HANDLE_P2P:
                setP2pChatManager((ChatManager) msg.obj);
                break;
        }
        return true;
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {

        // updating the necessary global lists
        updatePeerLists(peers);

        // if we are the group owner we send the invitation to the newly found peer
        if (isConnected && isGroupOwner) {
            for (WifiP2pDevice device : peerList) {
                if (device.status == AVAILABLE && !device.isGroupOwner()) {
                    connecPeer(device.deviceAddress);
                }
            }
        }

        if (!isConnected && !SMARTSelected) {
            SMARTSelected = true;
            appendStatus("waiting for peer discovery: " + calculateWait() + " ms");
            delayHandler1.postDelayed(new Runnable() {
                public void run() {
                    WiFiP2pService newService = new WiFiP2pService();
                    newService.instanceName = "MAGNET";
                    connectP2p(newService);
                }
            }, calculateWait());
        }
    }

    private void updatePeerLists(WifiP2pDeviceList peers) {
        peerList.clear();                                                                           // Clear All elements because the peer changed may be caused by peer disappearance
        peerList.addAll(peers.getDeviceList());                                                     // Add all peers found to the peerList

        for (WifiP2pDevice device : peerList) {
            boolean deviceAv = false;
            for (WiFiP2pService service : listFragmentService) {
                if (device.deviceAddress.equals(service.device.deviceAddress)) {
                    deviceAv = true;
                    service.device = device;
                    break;
                }
            }

            if (!deviceAv) {
                WiFiP2pService aNewService = new WiFiP2pService();
                aNewService.device = device;
                aNewService.instanceName = null;
                aNewService.serviceRegistrationType = null;
                aNewService.setIntention(13);
                listFragmentService.add(aNewService);
            }
        }
        updateFragment();
        proximityGroupList.clear();
        for (WifiP2pDevice device : peerList) {
            if (device.isGroupOwner()) {
                proximityGroupList.add(device);
            }
        }
    }

    // Update Peer List
    public void updatePeerList() {
        // updating peer list
        Toast.makeText(WiFiServiceDiscoveryActivity.this, "Peer Changed", Toast.LENGTH_SHORT).show();
        manager.requestPeers(channel, this);
        wifiManager.startScan();
    }

    // IF the device is connected, this method will be called
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        appendStatus("Connection Info Changed" + p2pInfo.isGroupOwner);
        isGroupFormed = p2pInfo.groupFormed;
        Thread udpReceiverThread;

        /*
         * The group owner accepts connections using a server socket and then spawns a
		 * client socket for every client. This is handled by {@code
		 * GroupOwnerSocketHandler}
		 */

        if (p2pInfo.isGroupOwner) {
            isGroupOwner = true;
            appendStatus("Connected as group owner");
            /*try {
                threadHandlerP2p = new GroupOwnerSocketHandler((this).getHandler(), SERVER_PORT);
                threadHandlerP2p.start();
            } catch (IOException e) {
                Log.d("IPAddress", "Failed to create a server thread - " + e.getMessage());
            }

            DataGramReceiver dgReceiver = new DataGramReceiver(8027, (this).getHandler());
            dgReceiver.start();*/


            manager.requestGroupInfo(channel, new GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    cGroupSSID = group.getNetworkName();
                    groupedPeerList.clear();   // Clear All elements because the peer changed may be caused by peer disappearance
                    groupedPeerList.addAll(group.getClientList());     // Add all peers found to the peerList
                    appendStatus("Number of clients in the group: " + (groupedPeerList.size()));
                    appendStatus("Group SSID: " + group.getNetworkName());
                    appendStatus("Group Pass: " + group.getPassphrase());
                    Log.d("GroupPass", group.getNetworkName() + "    " + group.getPassphrase() + "    " + p2pMacAddress);

                    FileWriter fileLogConnectionP2P;
                    try {
                        fileLogConnectionP2P = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/p2pConnection" + ".txt", true);
                        String lineaddress = "";
                        for (WifiP2pDevice sad : groupedPeerList) {
                            lineaddress = lineaddress + sad.deviceAddress + " -- ";
                        }
                        fileLogConnectionP2P.write(Utils.getCurrentTimeStamp() + " P2P connection status: GroupOwner (" + p2pMacAddress + ") Number of Clients: " + groupedPeerList +
                                " ChatClientListSize: " + chatClientList.size() + "(SSID: " + group.getNetworkName() + ")" + lineaddress + " isWiFiConnected: " + isWifiConnected + "\n");
                        fileLogConnectionP2P.flush();
                        fileLogConnectionP2P.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (!servicePasscreated) {
                        Map<String, String> recordPass = new HashMap<String, String>();
                        recordPass.put("PassPhrase", "SSID:" + group.getNetworkName() + ":PassPhrase:" + group.getPassphrase());
                        serviceSSIDPass = WifiP2pDnsSdServiceInfo.newInstance(
                                "SSID:" + group.getNetworkName() + ":PassPhrase:" + group.getPassphrase(), SERVICE_REG_TYPE, recordPass);
                        manager.addLocalService(channel, serviceSSIDPass, new ActionListener() {

                            @Override
                            public void onSuccess() {
                                appendStatus("Added Local Service: " + "PassPhrase");
                                servicePasscreated = true;
                            }

                            @Override
                            public void onFailure(int error) {
                                appendStatus("Failed to add a service: " + desError(error));
                            }
                        });
                    }

                    /*try {
                        Log.d(TAG, "sleep 1 sec");
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "InterruptedException");
                    }*/

                    int previousSize = connectedDevicesMap.size();
                    if (previousSize < group.getClientList().size()) {
                        Log.d(TAG, "groupList size is increased");

                        for (WifiP2pDevice p : group.getClientList()) {
                            String firstFourOctectWFDMac = p.deviceAddress.substring(0, Math.min(p.deviceAddress.length(), 11));
                            if (!connectedDevicesMap.containsKey(firstFourOctectWFDMac)) {

                                while (p2pNotDetected) {
                                    connectedDevicesMap = getConnectedDevicesMap(firstFourOctectWFDMac, p.deviceName);
                                }
                                p2pNotDetected = true;

                            }
                        }

                    } else if (previousSize > group.getClientList().size()) {
                        Log.d(TAG, "groupList size is decreased");
                        HashMap<String, ArpDevice> map = new HashMap<String, ArpDevice>();
                        for (WifiP2pDevice p : group.getClientList()) {
//                            p.deviceAddress.getBytes()
                            String firstFourOctectWFDMac = p.deviceAddress.substring(0, Math.min(p.deviceAddress.length(), 11));
                            if (connectedDevicesMap.containsKey(firstFourOctectWFDMac)) {
                                ArpDevice device = connectedDevicesMap.get(firstFourOctectWFDMac);
                                map.put(firstFourOctectWFDMac, device);
                            }
                        }

                        connectedDevicesMap = map;
                    } else {
                        Log.d(TAG, "connectedDevicesMap and groupList sizes are the same - do nothing");
                    }

                    Log.d(TAG, "connectedDevicesMap readings...");
                    for (ArpDevice value : connectedDevicesMap.values()) {
                        Log.d(TAG, "MAC " + value.getMac() + " has IP " + value.getIp() + " and ifc " + value.getIfc());
                    }

                    for (ArpDevice dev : connectedDevicesMap.values()) {
                        if (dev.getIfc().equalsIgnoreCase("wifi")) {
                            Log.d(TAG, "trying to create TCP connection to " + dev.getIp());
                            try {
                                Thread goLegacyHandler = new ClientSocketHandler(getHandler(), InetAddress.getByName(dev.getIp()), 4545);
                                goLegacyHandler.start();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }
            });

            if (!udpReceiverCreated) {
                //receiver use NO address
                //InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                udpReceiverThread = new DataGramReceiver(5050, this, getHandler()); //prova passare fragment
                udpReceiverThread.start();
                udpReceiverCreated = true;
                appendStatus("GO UDP Receiver Created");
            }

            for (WifiP2pDevice device : peerList) {
                if (device.status == AVAILABLE && !device.isGroupOwner()) { // Send invitations if it is available (not connected not invited) sometimes show unavailable by mistake
                    appendStatus("GO: Invitation sent to client: " + device.deviceAddress);
                    connecPeer(device.deviceAddress);
                }
            }
            // if it is not a GO, run a client socket threadHandler
        } else if (isGroupFormed) {
            FileWriter fileLogConnectionP2P;
            try {
                fileLogConnectionP2P = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/p2pConnection" + ".txt", true);
                fileLogConnectionP2P.write(Utils.getCurrentTimeStamp() + " P2P connection status: Client (" + p2pMacAddress + ") isWiFiConnected: " + isWifiConnected +
                        " GroupOwnerAdress: " + p2pInfo.groupOwnerAddress.getHostAddress()+ "CurrentGSSID: " + currentAPSSID + "\n");
                fileLogConnectionP2P.flush();
                fileLogConnectionP2P.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            appendStatus("Connected as Client");
            isGroupOwner = false;
            wifiManager.startScan();
            if (!udpReceiverCreated) {
                //receiver use NO address
                udpReceiverThread = new DataGramReceiver(8027, this, getHandler());
                udpReceiverThread.start();
                udpReceiverCreated = true;
                appendStatus("Client UDP Receiver Created");
            }
        }
    }

    // Status of the program which will be shown at the bottom
    public void appendStatus(String status) {
        statusTxtView.append("\n" + status);
        final Layout layout = statusTxtView.getLayout();
        if (layout != null) {
            int scrollDelta = layout.getLineBottom(statusTxtView.getLineCount() - 1)
                    - statusTxtView.getScrollY() - statusTxtView.getHeight();
            if (scrollDelta > 0)
                statusTxtView.scrollBy(0, scrollDelta);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (manager != null && channel != null) {
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;

            case R.id.atn_info:
                if (manager != null && channel != null) {
                    Intent intent_info = new Intent(this, Device_Info.class);
                    startActivity(intent_info);
                }
                return true;

            case R.id.atn_intention:
                // This is now a Decision Making bottom
                HashMap<String, String> theDecisionResult = decideGroupConnection(WTAList);
                for (HashMap.Entry<String, String> entry : theDecisionResult.entrySet()) {
                    String clientMAC = entry.getKey();
                    String apSSID = entry.getValue();

                    //find the client IP to be able to search for that in chatManagerList
                    /*String clientIP = "";
                    for (WTAClass wtaclass : WTAList) {
                        if (wtaclass.getInterfaceName().equals(clientMAC)) {
                            clientIP = wtaclass.interfaceIP;
                            break;
                        }
                    }*/

                    magnetMessage newMessage = new magnetMessage(ALCON_AP_CONNECTION, apSSID, " ", p2pMacAddress, clientMAC);
                    if (dataGramSender == null) {
                        if (isGroupOwner)
                            dataGramSender = new DataGramSender(8027, broadcastAddress);
                        else
                            dataGramSender = new DataGramSender(5050, broadcastAddress);
                        dataGramSender.start();
                    }

                    dataGramSender.setMsg(newMessage);
                    appendStatus("A command to connect to:" + apSSID + " sent to: " + clientMAC);
                }
                return true;

            case R.id.atn_clear:

                FileWriter fileClearLog;
                try {
                    fileClearLog = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/wifiConnection" + ".txt", true);
                    fileClearLog.write(Utils.getCurrentTimeStamp() + "Application Exited" + "\n");
                    fileClearLog.flush();
                    fileClearLog.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    fileClearLog = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/p2pConnection" + ".txt", true);
                    fileClearLog.write(Utils.getCurrentTimeStamp() + "Application Exited" + "\n");
                    fileClearLog.flush();
                    fileClearLog.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    fileClearLog = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/messageSent" + ".txt", true);
                    fileClearLog.write(Utils.getCurrentTimeStamp() + "Application Exited" + "\n");
                    fileClearLog.flush();
                    fileClearLog.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    fileClearLog = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/messageReceived" + ".txt", true);
                    fileClearLog.write(Utils.getCurrentTimeStamp() + "Application Exited" + "\n");
                    fileClearLog.flush();
                    fileClearLog.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                appTerminate = true;
                if (manager != null && channel != null) {
                    manager.removeGroup(channel, new ActionListener() {

                        @Override
                        public void onFailure(int reasonCode) {
                            appendStatus("Group cannot be Removed: " + desError(reasonCode));
                        }

                        @Override
                        public void onSuccess() {
                            Log.d("OnExit", "Remove group successfull");
                            appendStatus("Group Removed");
                        }

                    });

                    if (isConnected) {
                        manager.cancelConnect(channel, new ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("OnExit", "cancel connect successfull");
                                appendStatus("Current Connection Terminated");
                            }

                            @Override
                            public void onFailure(int arg0) {
                                appendStatus("Current Connection not Terminated: " + desError(arg0));
                            }
                        });
                    }
                    manager.clearLocalServices(channel, new ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("OnExit", "Clear local services successfull");
                            appendStatus("Local Services Cleared");
                        }

                        @Override
                        public void onFailure(int arg0) {
                            appendStatus("Local Services not Cleared: " + desError(arg0));
                        }
                    });
                    manager.clearServiceRequests(channel, new ActionListener() {

                        @Override
                        public void onSuccess() {
                            Log.d("OnExit", "Clear service requests successful");
                            appendStatus("All Service Requests Cleared");
                            unregisterReceiver(receiver);
                            appendStatus("Receiver Unregistered");
                            proximityGroupList.clear();                // Group ID   <=> Mac Address of GO
                            peerList.clear();                    // WifiP2pDevice of peers found in the proximity
                            groupedPeerList.clear();            // WifiP2pDevice of peers in the group
                            chatClientList.clear();            // List of ChatManagers. A chatManager Object is needed for each client to send message
                            WTAList.clear();
                            listFragmentService.clear();
                            if (wifiManager != null) {
                                WifiInfo wifiinfo = wifiManager.getConnectionInfo();
                                if (wifiinfo.getSSID().contains("DIRECT")) {
                                    wifiManager.disconnect();
                                }
                            }
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        }

                        @Override
                        public void onFailure(int arg0) {
                            appendStatus("All Service Requests not Cleared: " + desError(arg0));
                        }
                    });
                }
                return true;

            case R.id.atn_removeGroup:
                if (manager != null && channel != null) {
                    manager.removeGroup(channel, new ActionListener() {

                        @Override
                        public void onFailure(int reasonCode) {
                            appendStatus("Failed to remove the group: " + desError(reasonCode));
                        }

                        @Override
                        public void onSuccess() {
                            appendStatus("Group Removed");
                        }
                    });
                }
                return true;

            case R.id.atn_exit:
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(0);
                return true;

            case R.id.atn_sendtest:
                magnetMessage message = new magnetMessage(TEST_GROUP_CONNECTION, "A test packet: ", "", p2pMacAddress, "BROADCAST");
                if (isGroupOwner) {
                    WTAList.clear();
                    requestClientApList();
                    if (!chatClientList.isEmpty()) {
                        for (ChatManager client : chatClientList) {
                            if (client.socketIsConnected()) {
                                client.writeObject(message);
                                appendStatus("Message Sent via TCP!");
                            }
                        }
                    }
                } else {
                    if (isConnected) {
                        if (dataGramSender == null) {
                            dataGramSender = new DataGramSender(5050, broadcastAddress);
                            dataGramSender.start();
                        }
                        dataGramSender.setMsg(message);
                        appendStatus("Message Sent via UDP!");
                    }
                    if (P2pChatManager != null && P2pChatManager.socketIsConnected()) {
                        P2pChatManager.writeObject(message);
                        appendStatus("Message Sent via TCP!");
                    }
                }
                return true;

            case R.id.atn_reserdiscovery:
                restartServiceDiscovery();
                return true;

            case R.id.atn_creategroup:
                manager.removeGroup(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        forcedCreateGroup = true;
                        createGroup();
                        appendStatus("Group Removed Manually");
                    }

                    @Override
                    public void onFailure(int reason) {
                        appendStatus("Cannot Remove the Group: " + desError(reason));
                    }
                });
                return true;

            case R.id.atn_updatefragment:
                //wifiManager.startScan();
                //apListRequested = true;
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setP2pChatManager(ChatManager chatManager) {
        P2pChatManager = chatManager;
        if (isGroupOwner) {
            // check not to add several same chat manager to the list
            for (Iterator<ChatManager> iterator = chatClientList.iterator(); iterator.hasNext(); ) {
                ChatManager CManager = iterator.next();
                if (CManager.getRemoteAddress().equals(chatManager.getRemoteAddress())) {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();
                }
            }
            chatClientList.add(chatManager);
            appendStatus("chatcleintList size: " + chatClientList.size());
            appendStatus(chatClientList.size() + "th peer address: " + chatManager.getRemoteAddress());
        } else {
            appendStatus("Connected as P2P peer to: " + chatManager.getRemoteAddress());
        }
    }

    public void requestClientApList() {
        if (dataGramSender == null) {
            if (isGroupOwner)
                dataGramSender = new DataGramSender(8027, broadcastAddress);
            else
                dataGramSender = new DataGramSender(5050, broadcastAddress);
            dataGramSender.start();
        }

        magnetMessage aMagnetMessage = new magnetMessage(ALCON_APLIST_REQUEST, "Salam", " ", p2pMacAddress, "BROADCAST");
        dataGramSender.setMsg(aMagnetMessage);
        appendStatus("APList requested via UDP!");
    }

    // Clearing the service requests and start a fresh discovery
    // discovery stops after connection but we need to continue discovery
    public void restartServiceDiscovery() {
        appendStatus("restarting service discovery");
        listFragmentService.clear();
        manager.stopPeerDiscovery(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                appendStatus("Peer Discovery Stopped!");
                manager.clearServiceRequests(channel, new ActionListener() {

                    @Override
                    public void onSuccess() {

                        appendStatus("All Service Requests Cleared");
                        //serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                        manager.addServiceRequest(channel, serviceRequest, new ActionListener() {

                            @Override
                            public void onSuccess() {
                                appendStatus("Added service discovery request");
                                manager.discoverServices(channel, new ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        appendStatus("Service discovery initiated");
                                    }

                                    @Override
                                    public void onFailure(int arg0) {
                                        appendStatus("Service discovery failed: " + desError(arg0));
                                    }
                                });
                            }

                            @Override
                            public void onFailure(int arg0) {
                                appendStatus("Failed adding service discovery request: " + desError(arg0));
                            }
                        });
                    }

                    @Override
                    public void onFailure(int arg0) {
                        appendStatus("All Service Requests not Cleared: " + desError(arg0));
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                appendStatus("Failed to stop discovery: " + desError(reason));

            }

        });
    }

    //Updating list of wifi APs
    public void updateWifiAPs() {

        List<ScanResult> wifiScanResults = wifiManager.getScanResults();
        wifiAPList.clear();
        directAPList.clear();
        for (int i = 0; i < wifiScanResults.size(); i++) {
            wifiAPList.add(wifiScanResults.get(i).SSID);
            if (wifiScanResults.get(i).SSID.contains("DIRECT")) {
                ScanResultMagnet newDrResult = new ScanResultMagnet();
                newDrResult.BSSID = wifiScanResults.get(i).BSSID;
                newDrResult.capabilities = wifiScanResults.get(i).capabilities;
                newDrResult.frequency = wifiScanResults.get(i).frequency;
                newDrResult.level = wifiScanResults.get(i).level;
                newDrResult.SSID = wifiScanResults.get(i).SSID;
                directAPList.add(newDrResult);
            }
        }
        if (apListRequested) {
            apListRequested = false;
            magnetMessage responseMessage = new magnetMessage();
            responseMessage.what = ALCON_APLIST_RESPONSE;
            responseMessage.object = directAPList;
            responseMessage.senderIP = " ";
            responseMessage.senderMAC = p2pMacAddress;
            responseMessage.receiverMAC = apListReByMac;

            if (dataGramSender == null) {
                if (isGroupOwner)
                    dataGramSender = new DataGramSender(8027, broadcastAddress);
                else
                    dataGramSender = new DataGramSender(5050, broadcastAddress);
                dataGramSender.start();
            }
            dataGramSender.setMsg(responseMessage);
            appendStatus("AP List Sent");
        }
    }

    public String desError(int errorCode) {
        String returnState;
        switch (errorCode) {
            case 0:
                returnState = "internal error";
                break;
            case 1:
                returnState = " p2p unsupported";
                break;
            case 2:
                returnState = "framework busy";
                break;
            case 3:
                returnState = "no service requests";
                break;
            default:
                returnState = "Unknown error!";
        }
        return returnState;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onRestart() {
        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null) {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {
        listFragmentService.clear();
        super.onStop();
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Here the Group Owner decides intelligently and tell each client to connect to what other APs(GOs)
    private HashMap<String, String> decideGroupConnection(List<WTAClass> interfaceList) {

        appendStatus("decideGroupConnection");
        Node root = new Node("root");

        List<WTAClass> newInterfaceList = new ArrayList<WTAClass>();
        for (WTAClass tempWTA : interfaceList) {
            if (tempWTA.getGroupSeen().size() > 0) {
                newInterfaceList.add(tempWTA);
            }
        }

        // Now instantiate a Tree with the above test information
        Tree tree = new Tree("Tree0", root);
        tree.generateTree(newInterfaceList);

        // Extracting the List of all external proximity groups from interfaceList
        List<String> groupsInInterfaceList = new ArrayList<String>();
        for (WTAClass tempWTA : newInterfaceList) {
            for (ScanResultMagnet tempString : tempWTA.getGroupSeen()) {
                if (!groupsInInterfaceList.contains(tempString.SSID)) {
                    groupsInInterfaceList.add(tempString.SSID);
                }
            }
        }
        appendStatus("List all external Groups: " + "/n" + groupsInInterfaceList);
        Log.d("finalResult", "List all external Groups: " + "/n" + groupsInInterfaceList);
        // Find possible solutions by passing the tree to the findNodeDFS method
        List<HashMap<String, String>> finalResult = findNodeDFS(root);

        List<String> possibleGroups = new ArrayList<String>();
        for (HashMap<String, String> possibleG : finalResult) {
            Iterator it = possibleG.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
                if (!possibleGroups.contains(pair.getValue())) {
                    possibleGroups.add(pair.getValue());
                }
            }
        }
        // Print the reruned solutions (finalResult => normal; finalResultB => human redeable format)
        Log.d("finalResult", finalResult.size() + " different combination(s) are possible to connect to maximum " +
                possibleGroups.size() + " groups (out of " + groupsInInterfaceList.size() + ")" +
                " by means of " + newInterfaceList.size() + " interfaces" + "\n" + finalResult);
        appendStatus("final result size: " + finalResult.size());

        for (HashMap<String, String> hash : finalResult) {
            Iterator it = hash.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                Log.d("finalResult", pair.getKey() + " => " + pair.getValue());
                //it.remove(); // avoids a ConcurrentModificationException
            }
        }

        // For now for simplicity we only send back one possible choice
        appendStatus(finalResult.toString());
        return finalResult.get(0);

		/*if(finalResult.size()>1) {
            // Pass all possible solutions to the solutionOptimizer method to find the optimized one based on user defined metrics
			// newInterfaceList => List<WTAClass>  of clients
			// finalResult => List<HashMap<String, String>> of (client MAC address, GO SSID)
			HashMap<String, String> optimizedSolution = solutionOptimizer(finalResult, newInterfaceList);
			Log.d("optimizedSolution", String.valueOf(optimizedSolution.size()));
			return optimizedSolution;
		}else{
			return finalResult.get(0);
		}*/
    }

    public static List<HashMap<String, String>> findNodeDFS(final Node root) {
        int stage = 0;
        int maxSize = 0;
        String[] pathList = new String[500];
        List<HashMap<String, String>> finalSolution = new ArrayList<HashMap<String, String>>();

        @SuppressWarnings("serial")
        Stack<Node> stack = new Stack<Node>() {{
            add(root);
        }};
        while (!stack.isEmpty()) {
            Node current = stack.pop();
            String mydata = current.nodeName;

            // Extracting the Stage that the current node is in
            Pattern pattern = Pattern.compile("_(.*?)_");
            Matcher matcher = pattern.matcher(mydata);
            if (matcher.find()) {
                stage = Integer.parseInt((matcher.group(1).substring(0, 1)));
            }

            // Extracting the real external group name (nodes with the same group names should not be counted twice in the path)
            //externalGroup = mydata.substring(mydata.lastIndexOf('_') + 1).trim();

            //Add the current node name to the right stage in the pathList
            pathList[stage] = mydata;

            // count the number of groups in the path when we reach the leaf
            if (current.childNodeList.isEmpty()) {
                //System.out.println("Reach the leaf" + mydata + " " + stage);
                HashMap<String, String> tempArray = new HashMap<String, String>();
                for (int i = 1; i < stage + 1; i++) {
                    // we put the node in a hash map. the key values are the real name of external groups
                    // if there is any duplicates the previous value would be replaces
                    // later when we count the size of the hashmap it would return the correct size for us
                    String s = pathList[i];
                    s = s.substring(s.indexOf("(") + 1);
                    s = s.substring(0, s.indexOf(")"));

                    tempArray.put(pathList[i].substring(pathList[i].lastIndexOf('_') + 1).trim(), s);

                }
                if (tempArray.size() == maxSize) {
                    finalSolution.add(tempArray);
                    //System.out.println(finalSolution);
                }
                if (tempArray.size() > maxSize) {
                    maxSize = tempArray.size();
                    finalSolution.clear();
                    finalSolution.add(tempArray);
                }
            }
            stack.addAll(current.childNodeList);
        }
        return finalSolution;
    }

    // newInterfaceList => List<WTAClass> of clients
    // finalResult => List<HashMap<String, String>> of (client MAC address, GO SSID)
    public static HashMap<String, String> solutionOptimizer(List<HashMap<String, String>> finalResultB, List<WTAClass> interfaceList) {
        HashMap<String, String> optimizedSolution = new HashMap<String, String>();
        double wtaResult = 0;
        for (HashMap<String, String> tempHash : finalResultB) {
            Iterator<Entry<String, String>> it = tempHash.entrySet().iterator();
            double newdouble = 0;
            while (it.hasNext()) {
                @SuppressWarnings("rawtypes")
                HashMap.Entry pairs = (HashMap.Entry) it.next();
                double RSSI = 0;
                double groupValue = 1;

                // getting the RSSI and group Value for current pairs (connection)
                for (WTAClass tempWTA : interfaceList) {
                    if (tempWTA.getInterfaceName().equals(pairs.getValue())) {
                        RSSI = (double) tempWTA.RSSIMap.get(pairs.getKey());
                        groupValue = (double) tempWTA.groupValue.get(pairs.getKey());

                    }
                }
                // calculating one of the line of the algorithm (Weapon Target Assignment)
                newdouble = newdouble + groupValue * (1 - (RSSI / 100));

                //pairs.getKey()  pairs.getValue();
                //it.remove(); // avoids a ConcurrentModificationException
            }


            if (wtaResult == 0 || newdouble < wtaResult) {
                wtaResult = newdouble;
                optimizedSolution = tempHash;
                //System.out.println(wtaResult + " " + optimizedSolution);

            }
        }
        return optimizedSolution;
    }

    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    @Override
    public void onChannelDisconnected() {
        appendStatus("channel disconnected; trying to reconnect");
        channel = manager.initialize(this, getMainLooper(), this);
    }

    // This method will be called by Broadcast receiver when wifip2p state changed
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        if (isWifiP2pEnabled) {
            appendStatus("WiFi P2P is enabled");
        } else {
            appendStatus("WiFi P2P is disabled");
        }
    }

    //This method will be called by Broadcast receiver when connection status changed
    public void setConnectChanged(boolean connectionState) {
        isConnected = connectionState;

        if (isConnected) { // if true
            appendStatus("Connected!");
            appendStatus("My P2P IP: " + getIpAddress());
            alreadyConnected = true;
        } else {          // if false
            appendStatus("Disconnected");
            isGroupOwner = false;
            isGroupFormed = false;
            SMARTSelected = false;
            P2pChatManager = null;
            cGroupSSID = "";
            listFragmentService.clear();
            proximityGroupList.clear();
            peerList.clear();
            chatClientList.clear();
            groupedPeerList.clear();
            WTAList.clear();
            if (udpReceiverThread != null) {
                udpReceiverThread.interrupt();
                udpReceiverThread = null;
                udpReceiverCreated = false;
                appendStatus("Previous UDP Receiver Deleted");
            }
            if (dataGramSender != null) {
                dataGramSender.running = false;
                dataGramSender = null;
                appendStatus("Previous DataGramSender Deleted");
            }
            if (servicePasscreated) {
                manager.removeLocalService(channel, serviceSSIDPass, null);
                servicePasscreated = false;
                appendStatus("PassPhrase Service remove due to disconnection");
            }

            //Restart Discovery only if the disconnection was not requested by the user and we had already connected
            if (alreadyConnected && !appTerminate) {
                alreadyConnected = false;
                appendStatus("wait before restarting service discovery: 3000 ms");
                delayHandler3.postDelayed(new Runnable() { // wait. if not connected restart service discovery
                    public void run() {
                        if (!isConnected) {
                            appendStatus("Restarting service Discovery");
                            restartServiceDiscovery();
                        }
                    }
                }, 3000);
            }
        }
    }

    public void setDevicestatuschanged(int srcdevicestatus) {
        switch (srcdevicestatus) {
            case CONNECTED:
                appendStatus("Device Status: CONNECTED");
                break;
            case INVITED:
                appendStatus("Device Status: INVITED");
                break;
            case FAILED:
                appendStatus("Device Status: FAILED");
                break;
            case AVAILABLE:
                appendStatus("Device Status: AVAILABLE");
                break;
            case UNAVAILABLE:
                appendStatus("Device Status: UNAVAILABLE");
                break;
            default:
                appendStatus("Device Status: Unknown");
                break;
        }
    }

    //This method will be called by Broadcast receiver when WiFi connection status changed
    public void WifiConnectionChanged() {
        if (wifiManager != null) {
            WifiInfo wifiinfo = wifiManager.getConnectionInfo();
            String ip = Formatter.formatIpAddress(wifiinfo.getIpAddress());

            isWifiConnected = wifiinfo.getSSID().contains("DIRECT");

            FileWriter fileLogConnectionWifi;
            try {
                fileLogConnectionWifi = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/wifiConnection" + ".txt", true);
                fileLogConnectionWifi.write(Utils.getCurrentTimeStamp() + " W-Fi connection status: IsConnected:" + isWifiConnected + " SSID: " + wifiinfo.getSSID() + " isGroupOwner: " +
                        isGroupOwner + " isConnected: " + isConnected + " CurrentGroupSSID: " + currentAPSSID+ "\n");
                fileLogConnectionWifi.flush();
                fileLogConnectionWifi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!isGroupOwner && wifiinfo.getSSID().contains("DIRECT") && !threadAlreadyCreated && !ip.equalsIgnoreCase("0.0.0.0")) {
                currentAPSSID = wifiinfo.getSSID();
                //ip = Formatter.formatIpAddress(wifiinfo.getIpAddress());
                int port = 4545; // use this port to create a GO socket handler - maybe included in group advertising
                try {
                    legacyHandler = new GroupOwnerSocketHandler(getHandler(), port);
                    legacyHandler.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                threadAlreadyCreated = true;
            } else if (!wifiinfo.getSSID().equals(currentAPSSID) && threadAlreadyCreated) {
                threadAlreadyCreated = false;
                if (legacyHandler != null) {
                    legacyHandler.interrupt();
                    legacyHandler = null;
                    appendStatus("Previous LegacyHandler Deleted");
                }
            }
        }

    }

    //getter and setter for Handler
    public Handler getHandler() {
        return handler;
    }

    public void setP2pMacAddress(String address) {
        p2pMacAddress = address;
    }

    // Calculate Wating time for being second or ... Intention dynamically based on Intention
    private int calculateWait() {
        return (int) ((-5 * intention) + 75) * waitcoeff; // it will return waiting time Intention=14 => wait 5s, Inten = 1 => wait 70s
    }

    private void updateFragment() {
        WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
                .findFragmentByTag("services");
        if (fragment != null) {
            WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment.getListAdapter());
            adapter.clear();
            adapter.addAll(listFragmentService);
            adapter.notifyDataSetChanged();
        }
    }

    public void DiscoveryState(boolean discoverystate) {
        if (discoverystate) {
            appendStatus("P2P Discovery Started");
        } else {
            appendStatus("P2P Discovery Stopped");
        }
    }

    public static String getIpAddress() {
        try {
            List<NetworkInterface> interfaces = Collections
                    .list(NetworkInterface.getNetworkInterfaces());
        /*
         * for (NetworkInterface networkInterface : interfaces) { Log.v(TAG,
         * "interface name " + networkInterface.getName() + "mac = " +
         * getMACAddress(networkInterface.getName())); }
         */
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().contains("p2p"))
                    continue;
                List<InetAddress> addrs = Collections.list(intf
                        .getInetAddresses());
                for (InetAddress addr : addrs) {
                    // Log.v(TAG, "inside");

                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (isIPv4) {
                            if (sAddr.contains("192.168.49.")) {
                                return sAddr;
                            }
                        }

                    }

                }
            }

        } catch (Exception ex) {
            Log.v(TAG, "error in parsing");
        } // for now eat exceptions
        Log.v(TAG, "returning empty ip address");
        return "";
    }

    public static String getIPFromMac(String MAC) {
        BufferedReader br = null;
        String firstFourOctectMac = MAC.substring(0, Math.min(MAC.length(), 11));
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {

                String[] splitted = line.split(" +");
                if (splitted.length >= 4) {
                    // Basic sanity check
                    String device = splitted[5];
//                   if (device.matches(".*p2p-p2p0.*")){
                    if (device.contains("p2p")) {
                        String mac = splitted[3];
                        if (mac.startsWith(firstFourOctectMac)) { // prima era mac.matches(MAC)
                            return splitted[0];
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public HashMap<String, ArpDevice> getConnectedDevicesMap(String firstFourOctectWFDMac, String deviceName) {

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    if (!splitted[0].equalsIgnoreCase("ip")) { // skip first line
                        if (splitted[5].contains("p2p") && !splitted[3].equalsIgnoreCase("00:00:00:00:00:00")) {
                            // search for p2p with valid mac & ip

                            String firstFourOctectWiFiMac = splitted[3].substring(0, Math.min(splitted[3].length(), 11));

                            if (firstFourOctectWiFiMac.equalsIgnoreCase(firstFourOctectWFDMac)) {
                                String ifc = "p2p";
                                if (deviceName.equalsIgnoreCase(""))
                                    ifc = "wifi";
                                ArpDevice connectedDevice = new ArpDevice(splitted[3], splitted[0], ifc);
                                connectedDevicesMap.put(firstFourOctectWiFiMac, connectedDevice);
                                Log.d(TAG, "ho scritto la key: " + firstFourOctectWiFiMac);
                                p2pNotDetected = false;
                            }

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connectedDevicesMap;
    }

    public void sendMessage(String msg, String MacAddress) {
        magnetMessage newMessage = new magnetMessage(DELIVER_MESSAGE, msg, Utils.getIPAddress(true), p2pMacAddress, MacAddress);
        newMessage.messageID = new Random().nextLong();
        newMessage.timeStamp = Utils.getCurrentTimeStamp();
        if (isGroupOwner) {
            if (dataGramSender == null) {
                dataGramSender = new DataGramSender(8027, broadcastAddress);
                dataGramSender.start();
            }
            dataGramSender.setMsg(newMessage);

            if (!chatClientList.isEmpty()) {
                for (ChatManager chatManager : chatClientList) {
                    chatManager.writeObject(newMessage);
                }
            }
        } else if (isConnected && !isWifiConnected) {
            if (dataGramSender == null) {
                dataGramSender = new DataGramSender(5050, broadcastAddress);
                dataGramSender.start();
            }
            dataGramSender.setMsg(newMessage);
        } else if (isConnected && isWifiConnected) {
            if (dataGramSender == null) {
                dataGramSender = new DataGramSender(5050, broadcastAddress);
                dataGramSender.start();
            }
            dataGramSender.setMsg(newMessage);
            if (P2pChatManager != null) {
                P2pChatManager.writeObject(newMessage);
            }
        }

        FileWriter fileLogMessageSent;
        try {
            fileLogMessageSent = new FileWriter(Environment.getExternalStorageDirectory().getPath() + "/Alarms/" + testNum + "/messageSent" + ".txt", true);
            fileLogMessageSent.write("Sent on: " + Utils.getCurrentTimeStamp() + " ID: " + newMessage.messageID + "\n");
            fileLogMessageSent.flush();
            fileLogMessageSent.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
