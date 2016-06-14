
package com.example.android.wifidirect.discovery;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientSocketHandler extends Thread {

	private static final String TAG = "ClientSocketHandler";
	private Handler handler;
	private InetAddress mAddress;
	private int SERVER_PORT;

	public ClientSocketHandler(Handler handler, InetAddress groupOwnerAddress, int SERVER_PORT) {
		this.handler = handler;
		this.mAddress = groupOwnerAddress;
		this.SERVER_PORT = SERVER_PORT;
	}

	@Override
	public void run() {
		Socket socket = new Socket();
		try {
			socket.bind(null);
			socket.connect(new InetSocketAddress(mAddress.getHostAddress(), SERVER_PORT), 5000);
			Log.d(TAG, "Launching the I/O handler");
			new Thread(new ChatManager(socket, handler)).start();

		} catch (IOException e) {
			e.printStackTrace();
			try {
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return;
		}
		if (Thread.interrupted()) {
			// We've been interrupted: no more crunching.
			try {
				if (socket != null && !socket.isClosed())
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}
}
