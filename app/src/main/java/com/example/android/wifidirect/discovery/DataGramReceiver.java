package com.example.android.wifidirect.discovery;

import android.os.Handler;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by Francesco on 19/04/2016.
 */
public class DataGramReceiver extends Thread{

    private byte[] buffer = new byte[256];
    private DatagramSocket socket= null;
    private int port;
    private Handler handler;
    private WiFiServiceDiscoveryActivity activity;

    public DataGramReceiver(int port, WiFiServiceDiscoveryActivity activity, Handler handler) {
        this.handler = handler;
        this.activity = activity;
        this.port = port;
        try {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
        } catch (Exception e) {
            System.err.println("Connection failed. " + e.getMessage());
        }

    }

    @Override
    public void run() {
        Log.d("DataGramSocket", "DataGramReceiver started!");
        while (true) {
            try {
                byte[] buf = new byte[2000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                final Object receivedObj = Serializer.deserialize(packet.getData());
                handler.obtainMessage(WiFiServiceDiscoveryActivity.OBJECT_READ, receivedObj).sendToTarget();
                /*activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //stuff that updates ui
                        //chatFragment.pushMessage("Buddy: " + message.toString());
                        handler.obtainMessage(WiFiServiceDiscoveryActivity.OBJECT_READ, receivedObj).sendToTarget();
                    }
                });*/

               /* if(message.contains("peer") && message.split("\\|")[0].equalsIgnoreCase("peer")){
                    String addressForTcp = message.split("\\|")[1];
                    activity.startSocketWithLegacyGO(addressForTcp);
                }*/
                if (Thread.interrupted()) {
                    // We've been interrupted: no more crunching.
                    if (socket != null && !socket.isClosed())
                        socket.close();
                    return;
                }

//                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
