
package com.example.android.wifidirect.discovery;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Handles reading and writing of messages with socket buffers. Uses a Handler
 * to post messages to UI thread for UI updates.
 */
public class ChatManager implements Runnable {

	private Socket socket = null;
	private Handler handler;

	public ChatManager(Socket socket, Handler handler) {
		this.socket = socket;
		this.handler = handler;
	}

	private ObjectOutputStream oostream;
	private static final String TAG = "ChatHandler";

	@Override
	public void run() {
		try {
			InputStream iStream = socket.getInputStream();
			OutputStream oStream = socket.getOutputStream();
			oostream = new ObjectOutputStream(oStream);
			ObjectInputStream oistream = new ObjectInputStream(iStream);
			Object objectStream;

			handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE_P2P, this)
			.sendToTarget();

			while (true) {
				try {
					objectStream = oistream.readObject();
					Log.d(TAG, "Rec:" + String.valueOf(objectStream));
					handler.obtainMessage(WiFiServiceDiscoveryActivity.OBJECT_READ, objectStream).sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
				}
				catch (ClassNotFoundException e) {
					Log.e(TAG, "disconnected", e);
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void writeObject(Object object){
		try {
			oostream.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Exception during write object", e);
		}
	}

	public String getRemoteAddress(){
		Log.d("Test2", socket.getInetAddress().toString() + socket.getLocalSocketAddress().toString() + socket.getRemoteSocketAddress().toString());
		return socket.getInetAddress().toString();
	}
	public String getLocalAddress(){
		return socket.getLocalAddress().toString();
	}

	public boolean socketIsConnected(){
		return socket.isConnected();
	}
}
