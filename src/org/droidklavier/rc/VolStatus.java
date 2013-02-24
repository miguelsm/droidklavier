package org.droidklavier.rc;

public class VolStatus {
	public static final int MAIN_ACOUSTIC = 0;
	public static final int MAIN_QUIET = 1;
	public static final int MAIN_HEADPHONES = 2;
	public static final int VOICE = 3;
	public static final int TG = 4;
	public static final int AUDIO = 5;
	public static final int MIC = 6;

	private int mainAcoustic = 0;
	private int mainQuiet = 0;
	private int mainHeadphones = 0;
	private int voice = 0;
	private int tg = 0;
	private int audio = 0;
	private int mic = 0;

	protected int getVolume(int volumeType) {
		switch (volumeType) {
		case MAIN_ACOUSTIC:
			return mainAcoustic;

		case MAIN_QUIET:
			return mainQuiet;

		case MAIN_HEADPHONES:
			return mainHeadphones;

		case VOICE:
			return voice;

		case TG:
			return tg;

		case AUDIO:
			return audio;

		case MIC:
			return mic;
		}

		return 0;
	}

	protected void setVolume(int volumeType, int value) {
		if (value >= 0 && value <= 127) {
			switch (volumeType) {
			case MAIN_ACOUSTIC:
				mainAcoustic = value;
				break;

			case MAIN_QUIET:
				mainQuiet = value;
				break;

			case MAIN_HEADPHONES:
				mainHeadphones = value;
				break;

			case VOICE:
				voice = value;
				break;

			case TG:
				tg = value;
				break;

			case AUDIO:
				audio = value;
				break;

			case MIC:
				mic = value;
				break;
			}
		}
	}

	protected static String getVolumeName(int volumeType) {
		switch (volumeType) {
		case MAIN_ACOUSTIC:
			return "main_acoustic";

		case MAIN_QUIET:
			return "main_quiet";

		case MAIN_HEADPHONES:
			return "main_headphones";

		case VOICE:
			return "voice";

		case TG:
			return "tg";

		case AUDIO:
			return "audio";

		case MIC:
			return "mic";
		}

		return "";
	}
}
