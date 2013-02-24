package org.droidklavier.dialog;

import org.droidklavier.Droidklavier;
import org.droidklavier.R;
import org.droidklavier.rc.RC;
import org.droidklavier.rc.VolStatus;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class BalanceDialog extends DialogFragment {

	private Droidklavier mDroidklavier;
	private RC mRC;

	private SeekBar mSeekBarVoice;
	private SeekBar mSeekBarTG;
	private SeekBar mSeekBarAudio;
	private SeekBar mSeekBarMic;
	private TextView mTextVoice;
	private TextView mTextTG;
	private TextView mTextAudio;
	private TextView mTextMic;

	public BalanceDialog() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_balance, container);
		getDialog().setTitle(R.string.balance);

		mDroidklavier = (Droidklavier) getActivity().getApplication();
		mRC = mDroidklavier.getRC();

		int voiceVol = mRC.getVolume(VolStatus.VOICE);

		mTextVoice = (TextView) view.findViewById(R.id.balance_voice_value);
		mTextVoice.setText(String.valueOf(voiceVol));

		mSeekBarVoice = (SeekBar) view.findViewById(R.id.balance_voice);
		mSeekBarVoice.setProgress(voiceVol);
		mSeekBarVoice.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDroidklavier.sendTCPMessage(mRC.setVolume(VolStatus.VOICE,
						mSeekBarVoice.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mTextVoice.setText(String.valueOf(progress));
			}
		});

		int tgVol = mRC.getVolume(VolStatus.TG);

		mTextTG = (TextView) view.findViewById(R.id.balance_tg_value);
		mTextTG.setText(String.valueOf(tgVol));

		mSeekBarTG = (SeekBar) view.findViewById(R.id.balance_tg);
		mSeekBarTG.setProgress(tgVol);
		mSeekBarTG.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDroidklavier.sendTCPMessage(mRC.setVolume(VolStatus.TG,
						mSeekBarTG.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mTextTG.setText(String.valueOf(progress));
			}
		});

		int audioVol = mRC.getVolume(VolStatus.AUDIO);

		mTextAudio = (TextView) view.findViewById(R.id.balance_audio_value);
		mTextAudio.setText(String.valueOf(audioVol));

		mSeekBarAudio = (SeekBar) view.findViewById(R.id.balance_audio);
		mSeekBarAudio.setProgress(audioVol);
		mSeekBarAudio.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDroidklavier.sendTCPMessage(mRC.setVolume(VolStatus.AUDIO,
						mSeekBarAudio.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mTextAudio.setText(String.valueOf(progress));
			}
		});

		int micVol = mRC.getVolume(VolStatus.MIC);

		mTextMic = (TextView) view.findViewById(R.id.balance_mic_value);
		mTextMic.setText(String.valueOf(micVol));

		mSeekBarMic = (SeekBar) view.findViewById(R.id.balance_mic);
		mSeekBarMic.setProgress(micVol);
		mSeekBarMic.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDroidklavier.sendTCPMessage(mRC.setVolume(VolStatus.MIC,
						mSeekBarMic.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mTextMic.setText(String.valueOf(progress));
			}
		});

		return view;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {

		super.onDismiss(dialog);
		mDroidklavier.sendTCPMessage(RC.volStatus());
	}

}
