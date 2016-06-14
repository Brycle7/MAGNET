package com.example.android.wifidirect.discovery;

import java.io.Serializable;


public class magnetMessage implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int what;
	public Object object;
	public String senderIP = "";
	public String senderMAC = "";
	public String receiverMAC= "";
	public long messageID = 0;
	public String timeStamp;
	public int hopNum = 0;

	public magnetMessage(){

	}

	public magnetMessage(int what, Object object, String senderIP, String senderMAC, String receiverMAC) {
		this.what = what;
		this.object = object;
		this.senderIP = senderIP;
		this.senderMAC = senderMAC;
		this.receiverMAC = receiverMAC;
	}
}
