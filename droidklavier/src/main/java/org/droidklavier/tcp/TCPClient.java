package org.droidklavier.tcp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.droidklavier.BuildConfig;
import org.droidklavier.rc.RC;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient {

  private static final String TAG = "TCPClient";
  private final String HOST;
  private final int PORT;

  // Constants that indicate the current connection state
  public static final int STATE_CONNECTED = 0;
  public static final int STATE_DISCONNECTED = 1;

  private final Handler mHandler;
  private int mState;
  private Socket mSocket;
  private PrintWriter mWriter;
  private BufferedReader mReader;
  private ConnectedThread mConnectedThread;
  private KeepAliveThread mKeepAliveThread;

  public TCPClient(String host, int port, Handler handler) {
    HOST = host;
    PORT = port;
    mState = STATE_DISCONNECTED;
    mHandler = handler;
  }

  public synchronized boolean isConnected() {
    return mState == STATE_CONNECTED;
  }

  private synchronized void setState(int state) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, "setState() " + mState + " -> " + state);
    }

    mState = state;

    // Give the new state to the Handler so the UI Activity can update
    mHandler.obtainMessage(TCPHandler.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
  }

  public synchronized void sendMessage(String message) {
    ConnectedThread r;

    // Synchronize a copy of the ConnectedThread
    synchronized (this) {
      if (mState != STATE_CONNECTED) {
        return;
      }
      r = mConnectedThread;
    }

    // Perform the write unsynchronized
    r.write(message);
  }

  public synchronized void connect() {
    try {
      Log.e(TAG, "Connecting...");

      mSocket = new Socket();
      mSocket.connect(new InetSocketAddress(HOST, PORT), 10000);

      // send the message to the server
      mWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);

      // receive the message which the server sends back
      mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

      setState(STATE_CONNECTED);

    } catch (IOException e) {
      connectionFailed();
      Log.e(TAG, "Connection Error", e);
    }
  }

  public synchronized void stop() {
    setState(STATE_DISCONNECTED);
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread.interrupt();
      mConnectedThread = null;
    }
    if (mKeepAliveThread != null) {
      mKeepAliveThread.interrupt();
      mKeepAliveThread = null;
    }
  }

  public synchronized void connected() {
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    mConnectedThread = new ConnectedThread();
    mConnectedThread.start();
    mKeepAliveThread = new KeepAliveThread();
    mKeepAliveThread.start();

    // Send the address of connected device back to the UI Activity
    Message msg = mHandler.obtainMessage(TCPHandler.MESSAGE_CONNECTED_ADDRESS);
    Bundle bundle = new Bundle();
    bundle.putString(TCPHandler.CONNECTED_ADDRESS, HOST);
    msg.setData(bundle);
    mHandler.sendMessage(msg);
  }

  private void connectionLost() {
    if (BuildConfig.DEBUG) {
      Log.e(TAG, "Connection lost");
    }
    stop();
    mHandler.obtainMessage(TCPHandler.MESSAGE_CONNECTION_LOST).sendToTarget();
  }

  private void connectionFailed() {
    if (BuildConfig.DEBUG) {
      Log.e(TAG, "Connection failed");
    }
    stop();
    mHandler.obtainMessage(TCPHandler.MESSAGE_CONNECTION_FAILED).sendToTarget();
  }

  private class ConnectedThread extends Thread {
    public ConnectedThread() {
      setName("ConnectedThread");
    }

    @Override
    public void run() {
      String message;

      try {
        while (isConnected()) {
          message = mReader.readLine();
          if (message != null) {
            // Send the obtained bytes to the UI Activity
            mHandler.obtainMessage(TCPHandler.MESSAGE_READ,
                message.length(), -1, message).sendToTarget();
          }
          message = null;
        }
      } catch (Exception e) {
        connectionLost();
      } finally {
        cancel();
      }
    }

    public void write(String message) {
      if (mWriter != null && !mWriter.checkError()) {
        mWriter.println(message);
        mWriter.flush();

        // Share the sent message back to the UI Activity
        mHandler.obtainMessage(TCPHandler.MESSAGE_WRITE, -1, -1, message).sendToTarget();
      }
    }

    public void cancel() {
      try {
        mSocket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private class KeepAliveThread extends Thread {
    public KeepAliveThread() {
      setName("KeepAliveThread");
    }

    @Override
    public void run() {
      try {
        // Send <active /> message every 3 seconds
        while (isConnected()) {
          sleep(3000);
          sendMessage(RC.active());
        }
      } catch (InterruptedException e) {
        // e.printStackTrace();
      }
    }
  }
}
