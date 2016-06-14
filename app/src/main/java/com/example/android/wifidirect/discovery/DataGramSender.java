package com.example.android.wifidirect.discovery;

import android.app.Activity;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by Francesco on 19/04/2016.
 */
public class DataGramSender extends Thread{

    private DatagramSocket socket= null;
    private InetAddress address;
    private int port;
    private Object message;
    private boolean needToSend = false;
    volatile boolean running = true;

    public DataGramSender(final int port, final InetAddress address) {
        this.address = address;
        this.port = port;
        try {
            socket = new DatagramSocket(port);
            socket.setBroadcast(true);
            //socket.setSoTimeout(1000);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    socket.connect(address, port);
                    try {
                        socket.setSoTimeout(1000);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            });
            Log.d("DataGramSocket", "Initialize DataGramSender");
        } catch (Exception e) {
            System.err.println("Connection failed. " + e.getMessage());
        }

    }

    @Override
    public void run() {
        while(needToSend) {
            if(message!=null) {
                try {
                    byte[] buf = Serializer.serialize(message);
                    DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
                    socket.send(packet);
                    //            ((WiFiServiceDiscoveryActivity) activity).appendStatus("Salam Sent to: " + packet.getAddress());
//                    needToSend = false;
                    Log.d("DataGramSocket", message + " sent to: " + packet.getAddress());
                    message = null;
                    //Thread.sleep(1000);
                }
                catch (Exception e) {
                    System.err.println(e.getMessage());
                }
//                finally {
//                    if (socket != null)
//                        socket.close();
//                }
            }
        }
        if (!running) {
            // We've been interrupted: no more crunching.
            if (socket != null && !socket.isClosed())
                socket.close();
            return;
        }
    }

    public void setMsg(Object msg){
        message = msg;
        needToSend = true;
    }
}
