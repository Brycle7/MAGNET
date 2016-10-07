
package com.example.android.wifidirect.discovery;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * A structure to hold service information.
 */
public class WiFiP2pService {
	public WifiP2pDevice device;
	public String instanceName = "";
	public String serviceRegistrationType = "";
	private double intention = 0;
	public String SSID = "";
	public String PassPhrase = "";

	public double getIntention() {
		return intention;
	}

	public void setIntention(double intention) {
		this.intention = intention;
	}



	public WiFiP2pService(){

	}


}
