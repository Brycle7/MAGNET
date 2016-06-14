package com.example.android.wifidirect.discovery;
/*
This class is similar to Wifi ScanResult Class. However, since we need a simple serializable interface to be able to send
an arraylist of this class to the GO make it simple. Just important String and values are kept in this class
 */
import java.io.Serializable;

/**
 * Created by Naser on 1/20/2016.
 */
public class ScanResultMagnet implements Serializable {

    /** The network name. */
    public String SSID;

    /**
     * The detected signal level in dBm. At least those are the units used by
     * the TI driver.
     */
    public int level;

    /** The address of the access point. */
    public String BSSID;
    /**
     * Describes the authentication, key management, and encryption schemes
     * supported by the access point.
     */

    /**
     * The frequency in MHz of the channel over which the client is communicating
     * with the access point.
     */
    public int frequency;

    /**
     * Describes the authentication, key management, and encryption schemes
     * supported by the access point.
     */
    public String capabilities;

    @Override
    public String toString() {
        return "ScanResultMagnet{" +
                "SSID='" + SSID + '\'' +
                ", level=" + level +
                ", BSSID='" + BSSID + '\'' +
                ", frequency=" + frequency +
                ", capabilities='" + capabilities + '\'' +
                '}';
    }

    public ScanResultMagnet(){

    }

}
