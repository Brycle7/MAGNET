
package com.example.android.wifidirect.discovery;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * A structure to hold service information.
 */
public class WiFiP2pService {
	public WifiP2pDevice device;
	public String instanceName = null;
	public String serviceRegistrationType = null;
	private double intention;
	public String PassPhrase=null;

	public double getIntention() {
		return intention;
	}

	public void setIntention(double intention) {
		this.intention = intention;
	}



	public WiFiP2pService(){

	}


}
