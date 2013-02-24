package org.droidklavier.rc;

import org.droidklavier.crypto.Crypto;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class RC {

	public static final String TRC = "TRC-100";
	public static final int VERSION = 1130;

	public static final String QUIET_STATUS_ACOUSTIC = "acoustic";
	public static final String QUIET_STATUS_QUIET = "quiet";

	private VolStatus mVolStatus;

	public RC() {

		mVolStatus = new VolStatus();
	}

	public int getVolume(int volumeType) {

		return mVolStatus.getVolume(volumeType);
	}

	public String setVolume(int volumeType, int volume) {

		mVolStatus.setVolume(volumeType, volume);

		String volumeName = VolStatus.getVolumeName(volumeType);

		if (volumeName.length() > 0)
			return "<vol_status " + volumeName + "=\"" + volume + "\" />";

		return "";
	}

	public void setVolumes(int mainAcoustic, int mainQuiet, int mainHeadphone,
			int voice, int tg, int audio, int mic) {

		mVolStatus.setVolume(VolStatus.MAIN_ACOUSTIC, mainAcoustic);
		mVolStatus.setVolume(VolStatus.MAIN_QUIET, mainQuiet);
		mVolStatus.setVolume(VolStatus.MAIN_HEADPHONES, mainHeadphone);
		mVolStatus.setVolume(VolStatus.VOICE, voice);
		mVolStatus.setVolume(VolStatus.TG, tg);
		mVolStatus.setVolume(VolStatus.AUDIO, audio);
		mVolStatus.setVolume(VolStatus.MIC, mic);
	}

	public static String active() {

		return "<active />";
	}

	public static String play() {

		return "<play />";
	}

	public static String next() {

		return "<next />";
	}

	public static String prev() {

		return "<prev />";
	}

	public static String stop() {

		return "<stop />";
	}

	public static String pause() {

		return "<pause />";
	}

	public static String volStatus() {

		return "<vol_status />";
	}

	public static String quietStatus() {

		return "<quiet_status />";
	}

	public static String rcStatus() {

		return String.format("<rc_status rc=\"%1$s\" version=\"%2$d\" />", TRC,
				VERSION);
	}

	public static String getRcsStatus() {

		return "<get_rcs_status />";
	}

	public static String setRcsStatus(String status) {

		return String.format("<set_rcs_status status=\"%s\" />", status);
	}

	public static String rcCertificate(String param, String password) {

		return String.format("<rc_certificate param=\"%s\" />",
				Crypto.encrypt(param, password));
	}

	public static String quietStatus(String mode) {

		return String.format("<quiet_status mode=\"%s\" />", mode);
	}

	public static String loadSongSource(String source, int album_id,
			int displayOrder) {

		return String
				.format("<load_song source_id=\"%1$s\" album_id=\"%2$d\" sel_song_no=\"%3$d\" />",
						source, album_id, displayOrder);
	}

	public static String search(int position) {

		return String.format("<search position=\"%d\" />", position);
	}

	public static String songSearch(String sourceId, String keyword) {

		return String.format(
				"<song_search source_id=\"%1$s\" keyword=\"%2$s\" />",
				sourceId, keyword);
	}

	public static String fileFuncCancel(String func) {

		return String.format("<file_func_cancel func=\"%s\" />", func);
	}

}
