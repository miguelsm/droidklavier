package org.droidklavier;

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

public class Droidklavier extends Application {

	private static final int PORT = 3191;

	public static Context context;

	private SharedPreferences mPrefs;
	private TCPClient mTcpClient;
	private DAO mDAO;
	private final TCPHandler mTCPHandler = new TCPHandler(this);
	private final RC mRC = new RC();
	private Player mPlayer;
	private boolean mLoading;
	private ConnectTask mConnectTask;
	private String mConnectedAddr;

	@Override
	public void onCreate() {

		super.onCreate();
		context = getApplicationContext();
		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mDAO = new DAO(new DBClient(getHost(), getDBName(), getDBUser(),
				getDBPass()));
	}

	@Override
	public void onTerminate() {

		super.onTerminate();

		if (mTcpClient != null) {
			mTcpClient.stop();
		}
	}

	private String getHost() {

		return mPrefs.getString(SettingsActivity.HOST,
				SettingsActivity.HOST_DEFAULT);
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

			mDAO.setDBClient(new DBClient(getHost(), getDBName(), getDBUser(),
					getDBPass()));
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

	private class ConnectTask extends AsyncTask<String, Void, TCPClient> {

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

}
