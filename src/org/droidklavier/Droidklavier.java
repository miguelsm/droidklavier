package org.droidklavier;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.droidklavier.activity.Player;
import org.droidklavier.activity.SettingsActivity;
import org.droidklavier.db.DAO;
import org.droidklavier.db.DBClient;
import org.droidklavier.rc.RC;
import org.droidklavier.tcp.TCPClient;
import org.droidklavier.tcp.TCPHandler;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class Droidklavier extends Application {

	private static final int PORT = 3191;
	private static final String TAG = "Droidklavier";

	public static Context context;

	private SharedPreferences mPrefs;
	private TCPClient mTcpClient;
	private DAO mDAO;
	private final TCPHandler mTCPHandler = new TCPHandler(this);
	private final RC mRC = new RC();
	private Player mPlayer;
	private boolean mLoading;
	private ConnectTask mConnectTask;
	private UPNPSearchTask mUPNPTask;
	private String mConnectedAddr;
	private String foundHost = "";

	@Override
	public void onCreate() {

		super.onCreate();
		context = getApplicationContext();
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mDAO = new DAO(new DBClient(getHost(), getDBName(), getDBUser(), getDBPass()));
	}

	@Override
	public void onTerminate() {

		super.onTerminate();

		if (mTcpClient != null) {
			mTcpClient.stop();
		}
	}

	private String getHost() {
		if (foundHost != "") 
			return foundHost;
		return mPrefs.getString(SettingsActivity.HOST, SettingsActivity.HOST_DEFAULT);
	}

	private String getDBName() {

		return mPrefs.getString(SettingsActivity.DATABASE,
				SettingsActivity.DATABASE_DEFAULT);
	}

	private String getDBUser() {

		return mPrefs.getString(SettingsActivity.DB_USER,
				SettingsActivity.DB_USER_DEFAULT);
	}

	private String getDBPass() {

		return mPrefs.getString(SettingsActivity.DB_PASS,
				SettingsActivity.DB_PASS_DEFAULT);
	}

	public void connect() {
		if (!isConnected()) {
			mUPNPTask = new UPNPSearchTask();
			mUPNPTask.execute();

			mDAO.setDBClient(new DBClient(getHost(), getDBName(), getDBUser(), getDBPass()));

			mConnectTask = new ConnectTask();
			mConnectTask.execute();
		}
	}

	public void reconnect() {

		if (isConnected()) {
			disconnect();
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {

					connect();
				}
			}, 2000);
		} else {
			connect();
		}
	}

	public void disconnect() {

		if (isConnected()) {
			mTcpClient.stop();
		}
	}

	public boolean isConnected() {

		return mTcpClient != null && mTcpClient.isConnected();
	}

	public void sendTCPMessage(String message) {

		if (isConnected()) {
			mTcpClient.sendMessage(message);
		}
	}

	public DAO getDAO() {

		return mDAO;
	}

	public RC getRC() {

		return mRC;
	}

	public Player getPlayer() {

		return mPlayer;
	}

	public void setPlayer(Player player) {

		mPlayer = player;
	}

	public boolean isLoading() {

		return mLoading;
	}

	public void setLoading(boolean loading) {

		mLoading = loading;
	}

	public String getConnectedAddr() {

		return mConnectedAddr;
	}

	public void setConnectedAddr(String address) {

		mConnectedAddr = address;
	}

	private class ConnectTask extends AsyncTask<String, Void, TCPClient> 
	{
		@Override
		protected TCPClient doInBackground(String... params) {

			TCPClient client = new TCPClient(getHost(), PORT, mTCPHandler);
			client.connect();
			return client;
		}

		@Override
		protected void onPostExecute(TCPClient result) {
			super.onPostExecute(result);
			mTcpClient = result;
			if (isConnected()) {
				mTcpClient.connected();
			}
		}
	}
	
	private class UPNPSearchTask extends AsyncTask<String, Void, MyUPNPControlPoint> 
	{
		@Override
		protected MyUPNPControlPoint doInBackground(String... arg0) {
			MyUPNPControlPoint pnp = new MyUPNPControlPoint();
			return pnp;
		}

		protected void onPostExecute(MyUPNPControlPoint result) {
			super.onPostExecute(result);
			foundHost = result.ip;
			if (foundHost != "")
				mDAO.setHost(foundHost);
		}	
	}
	
	
	private class MyUPNPControlPoint extends ControlPoint implements DeviceChangeListener 
	{	
		private String ip = "";
		private String descrip;
		private boolean done = false;
		private boolean notified = false;
			
		public String getIP() {
			return ip;
		}
		
		public String getDescrip() {
			return descrip;
		}
		
		public MyUPNPControlPoint() {
			addDeviceChangeListener(this);
			StartAndWait();
		}
		
		private synchronized void StartAndWait(){
			start();
			
			Log.d(TAG, "Starting pnp search");
			try {
				wait(10000);
			}
			catch (InterruptedException e) {}
			Log.d(TAG, "Done with StartAndWait! IP is " + ip);		
			if(ip.equals("")) {
				DeviceList rootDevList = getDeviceList(); 
				int nRootDevs = rootDevList.size(); 
				for (int n=0; n<nRootDevs; n++) { 
					Device dev = rootDevList.getDevice(n); 
					String devName = dev.getFriendlyName();
					String IP = dev.getAbsoluteURL(dev.getDescriptionFilePath()).split(":")[1].replace("/", "");
					if(devName.startsWith("DKV") && dev.getManufacture().startsWith("YAMAHA") && dev.getModelName().contains("MarkIV")) {
						Log.d(TAG, "FOUND [" + n + "] = " + devName + "," + IP);
						ip = IP;
						descrip = dev.getModelName();
					}
				}
			}
			this.stop();
			
		}

		public void deviceAdded(Device dev) {
			String devName = dev.getFriendlyName();
			String IP = dev.getAbsoluteURL(dev.getDescriptionFilePath()).split(":")[1].replace("/", "");
//			Utils.verbose("added device = " + devName + "," + IP);
			if(devName.startsWith("DKV") && dev.getManufacture().startsWith("YAMAHA") && dev.getModelName().contains("MarkIV")) {
				Log.d(TAG, "Found " + devName + "," + IP);
				if (!done) {
					ip = IP;
					done = true;
					descrip = dev.getModelName();
					notified = true;
					notify();
				}
			} 
		}

		public void deviceRemoved(Device device) {					
		}		
	}
}
