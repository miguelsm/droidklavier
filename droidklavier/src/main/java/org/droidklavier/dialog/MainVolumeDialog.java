package org.droidklavier.dialog;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import org.droidklavier.Droidklavier;
import org.droidklavier.R;
import org.droidklavier.rc.RC;
import org.droidklavier.rc.VolStatus;

public class MainVolumeDialog extends DialogFragment {

  private Droidklavier mDroidklavier;
  private RC mRC;

  private SeekBar mSeekBarAcoustic;
  private SeekBar mSeekBarQuiet;
  private SeekBar mSeekBarHeadphones;
  private TextView mTextAcoustic;
  private TextView mTextQuiet;
  private TextView mTextHeadphones;

  public MainVolumeDialog() {

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_main_volume, container);
    getDialog().setTitle(R.string.main_volume);

    mDroidklavier = (Droidklavier) getActivity().getApplication();
    mRC = mDroidklavier.getRC();

    int acousticVol = mRC.getVolume(VolStatus.MAIN_ACOUSTIC);

    mTextAcoustic = (TextView) view.findViewById(R.id.main_volume_acoustic_value);
    mTextAcoustic.setText(String.valueOf(acousticVol));

    mSeekBarAcoustic = (SeekBar) view.findViewById(R.id.main_volume_acoustic);
    mSeekBarAcoustic.setProgress(acousticVol);
    mSeekBarAcoustic.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mDroidklavier.sendTCPMessage(mRC.setVolume(VolStatus.MAIN_ACOUSTIC, mSeekBarAcoustic.getProgress()));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextAcoustic.setText(String.valueOf(progress));
      }
    });

    int quietVol = mRC.getVolume(VolStatus.MAIN_QUIET);

    mTextQuiet = (TextView) view.findViewById(R.id.main_volume_quiet_value);
    mTextQuiet.setText(String.valueOf(quietVol));

    mSeekBarQuiet = (SeekBar) view.findViewById(R.id.main_volume_quiet);
    mSeekBarQuiet.setProgress(quietVol);
    mSeekBarQuiet.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mDroidklavier.sendTCPMessage(mRC.setVolume(VolStatus.MAIN_QUIET, mSeekBarQuiet.getProgress()));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextQuiet.setText(String.valueOf(progress));
      }
    });

    int headphonesVol = mRC.getVolume(VolStatus.MAIN_HEADPHONES);

    mTextHeadphones = (TextView) view.findViewById(R.id.main_volume_headphones_value);
    mTextHeadphones.setText(String.valueOf(headphonesVol));

    mSeekBarHeadphones = (SeekBar) view.findViewById(R.id.main_volume_headphones);
    mSeekBarHeadphones.setProgress(headphonesVol);
    mSeekBarHeadphones.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mDroidklavier.sendTCPMessage(mRC.setVolume(VolStatus.MAIN_HEADPHONES, mSeekBarHeadphones.getProgress()));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mTextHeadphones.setText(String.valueOf(progress));
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
