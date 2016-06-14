package com.example.android.wifidirect.discovery;

import java.util.HashMap;
import java.util.List;

import android.net.wifi.ScanResult;

public class WTAClass {

	private List<ScanResultMagnet> groupSeen;
	private String interfaceName;                 // the MAC address of this device
	public String interfaceIP;					  // THE ip ADDRESS of the device
	public HashMap <String, Integer> RSSIMap;     // <Group name, RSSI Value>  0 =< RSSI >= 100
	public HashMap <String, Integer> groupValue;  // <Group name, group Value (the value of target i in WTA)> ----  1 =< value >= 10
	
	public WTAClass(String name){
		setInterfaceName(name);
		RSSIMap = new HashMap<String, Integer>();
		groupValue = new HashMap<String, Integer>();
	}

	public List<ScanResultMagnet> getGroupSeen() {
		return groupSeen;
	}

	public void setGroupSeen(List<ScanResultMagnet> groupSeen) {
		this.groupSeen = groupSeen;
		
		// right now we do not have group value so the group values are all 1
		// later we will add a real value for each group based on real metrics like the number of other groups that this group is connected to
		for (ScanResultMagnet tempString: groupSeen){
			RSSIMap.put(tempString.SSID, tempString.level);
			groupValue.put(tempString.SSID, tempString.level);
		}
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
}
	