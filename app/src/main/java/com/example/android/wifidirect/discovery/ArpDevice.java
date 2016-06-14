package com.example.android.wifidirect.discovery;

/**
 * Created by Naser on 5/9/2016.
 */
public class ArpDevice {

    private String mac;
    private String ip;
    private String ifc;

    public ArpDevice(String mac, String ip, String ifc){
        this.mac = mac;
        this.ip = ip;
        this.ifc = ifc;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIfc() {
        return ifc;
    }

    public void setIfc(String ifc) {
        this.ifc = ifc;
    }

}
