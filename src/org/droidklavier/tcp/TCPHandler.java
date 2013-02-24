package org.droidklavier.tcp;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.droidklavier.BuildConfig;
import org.droidklavier.Droidklavier;
import org.droidklavier.R;
import org.droidklavier.activity.Player;
import org.droidklavier.activity.SettingsActivity;
import org.droidklavier.rc.RC;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * The Handler that gets information back from the TCPClient
 * 
 */
public class TCPHandler extends Handler {

	private static final String TAG = "TCPHandler";

	public static final int MESSAGE_STATE_CHANGE = 0;
	public static final int MESSAGE_READ = 1;
	public static final int MESSAGE_WRITE = 2;
	public static final int MESSAGE_CONNECTED_ADDRESS = 3;
	public static final int MESSAGE_CONNECTION_FAILED = 4;
	public static final int MESSAGE_CONNECTION_LOST = 5;

	// Key names received from the TCPClient
	public static final String CONNECTED_ADDRESS = "connected_address";

	private final Droidklavier mDroidklavier;
	private DocumentBuilder mDocumentBuilder;

	public TCPHandler(Droidklavier droidklavier) {

		mDroidklavier = droidklavier;

		/** DocumentBuilder for parsing incoming XML messages */
		try {

			mDocumentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

		} catch (Exception e) {

			if (BuildConfig.DEBUG) {
				Log.e(TAG, "XML Document: " + e.getMessage());
			}
		}
	}

	@Override
	public void handleMessage(Message msg) {

		super.handleMessage(msg);

		Player player;

		switch (msg.what) {

		case MESSAGE_STATE_CHANGE:

			handleStateChange(msg);
			break;

		case MESSAGE_WRITE:

			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Message sent: " + (String) msg.obj);
			}
			break;

		case MESSAGE_READ:

			handleMessageRead(msg);
			break;

		case MESSAGE_CONNECTED_ADDRESS:

			handleMConnectedAddr(msg);
			break;

		case MESSAGE_CONNECTION_FAILED:

			player = mDroidklavier.getPlayer();
			if (player != null) {
				player.connectionFailed();
			}
			break;

		case MESSAGE_CONNECTION_LOST:

			player = mDroidklavier.getPlayer();
			if (player != null) {
				player.connectionLost();
			}
			break;
		}
	}

	private synchronized void parse(String xmlString) throws Exception {

		Element element = mDocumentBuilder.parse(
				new InputSource(new StringReader(xmlString)))
				.getDocumentElement();

		if (element == null) {
			return;
		}

		String tagName = element.getTagName();

		// RCS_STATUS
		if (tagName.equals("rcs_status")) {
			parseRcsStatus(element);
			return;
		}
		// RC_CERTIFICATE
		if (tagName.equals("rc_certificate")) {
			parseRcCert(element);
			return;
		}
		// QUIET_STATUS
		if (tagName.equals("quiet_status")) {
			parseQuietStatus(element);
			return;
		}
		// VOL_STATUS
		if (tagName.equals("vol_status")) {
			parseVolStatus(element);
			return;
		}
		// KARAOKE
		// report when it has finished after sending <search />
		if (tagName.equals("karaoke")) {
			parseKaraoke();
			return;
		}
		// SEQ_STATUS
		if (tagName.equals("seq_status")) {
			parseSeqStatus(element);
			return;
		}
		// MESSAGE
		if (tagName.equals("message_box")) {
			parseMessageBox(element);
		}

		mDocumentBuilder.reset();
	}

	private void handleStateChange(Message msg) {

		if (BuildConfig.DEBUG) {
			Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
		}

		Player player;

		switch (msg.arg1) {

		case TCPClient.STATE_CONNECTED:

			break;

		case TCPClient.STATE_DISCONNECTED:

			player = mDroidklavier.getPlayer();
			if (player != null) {
				player.connectionLost();
			}
			break;
		}
	}

	private void handleMessageRead(Message msg) {

		String message = ((String) msg.obj).trim();

		try {
			if (message != null && message.length() > 0) {
				parse(message);
			}
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "XML Parser Error: " + e.getMessage());
			}
			e.printStackTrace();
		}

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "Message received: " + message);
		}
	}

	private void handleMConnectedAddr(Message msg) {

		String connectedAddr = msg.getData().getString(CONNECTED_ADDRESS);
		mDroidklavier.setConnectedAddr(connectedAddr);
		Player player = mDroidklavier.getPlayer();
		if (player != null) {
			player.connectionSuccess();
		}
	}

	private void parseRcsStatus(Element element) {

		Player player = mDroidklavier.getPlayer();
		if (player != null) {
			player.updateRCSStatus(element.getAttribute("status"));
		}
	}

	private void parseRcCert(Element element) {

		String param = element.getAttribute("param");
		String result = element.getAttribute("result");

		if (param.length() > 0) {
			SharedPreferences sharedPref = PreferenceManager
					.getDefaultSharedPreferences(Droidklavier.context);
			String pass = sharedPref.getString(SettingsActivity.PASS,
					SettingsActivity.PASS_DEFAULT);
			mDroidklavier.sendTCPMessage(RC.rcCertificate(param, pass));
			mDroidklavier.sendTCPMessage(RC.quietStatus());
			mDroidklavier.sendTCPMessage(RC.getRcsStatus());
		}

		if (BuildConfig.DEBUG) {
			if (result.equals("pass")) {
				Log.d(TAG, "RC Certificate = PASS");
			} else if (result.equals("fail")) {
				Log.e(TAG, "RC Certificate = FAIL");
			}
		}

	}

	private void parseQuietStatus(Element element) {

		String mode = element.getAttribute("mode");
		Player player = mDroidklavier.getPlayer();
		if (player != null) {
			player.updateQuietStatus(mode);
			mDroidklavier.sendTCPMessage(RC.volStatus());
		}
	}

	private void parseVolStatus(Element element) {

		mDroidklavier.getRC().setVolumes(
				Integer.parseInt(element.getAttribute("main_acoustic")),
				Integer.parseInt(element.getAttribute("main_quiet")),
				Integer.parseInt(element.getAttribute("main_headphone")),
				Integer.parseInt(element.getAttribute("voice")),
				Integer.parseInt(element.getAttribute("tg")),
				Integer.parseInt(element.getAttribute("audio")),
				Integer.parseInt(element.getAttribute("mic")));
	}

	private void parseKaraoke() {

		Player player = mDroidklavier.getPlayer();
		if (player != null) {
			player.updateSeekingStatus(false);
		}
	}

	private void parseSeqStatus(Element element) {

		String status = element.getAttribute("status");
		String source = element.getAttribute("source_id");
		int albumId = Integer.parseInt(element.getAttribute("album_id"));
		int selSongNo = Integer.valueOf(element.getAttribute("sel_song_no"));
		int time = Integer.parseInt(element.getAttribute("time"));

		Player player = mDroidklavier.getPlayer();
		if (player != null) {
			player.updateSeqStatus(status, source, albumId, selSongNo, time);
		}

		if (status.equals("loaded")) {
			mDroidklavier.sendTCPMessage(RC.play());
		}
	}

	private void parseMessageBox(Element element) {

		String message = "";
		NodeList messageList = element.getChildNodes();
		int length = messageList.getLength();

		for (int i = 0; i < length; i++) {
			message += messageList.item(i).getChildNodes().item(0)
					.getNodeValue();
			if (i < length - 1) {
				message += "\n";
			}
		}

		Player player = mDroidklavier.getPlayer();
		if (player != null && player.isSearching()) {
			if (element.getAttribute("button").equals("CANCEL")) {
				player.searchProcessing(message);
			} else if (element.getAttribute("button").equals("OK")) {
				if (element.getAttribute("rank").equals("error")) {
					player.searchEnd(
							mDroidklavier.getString(R.string.search_canceled),
							message);
				} else {
					player.searchEnd(
							mDroidklavier.getString(R.string.search_complete),
							message);
				}
			}
		}
	}

};