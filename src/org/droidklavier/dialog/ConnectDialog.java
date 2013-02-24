package org.droidklavier.dialog;

import org.droidklavier.R;
import org.droidklavier.activity.Player;
import org.droidklavier.activity.SettingsActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class ConnectDialog extends DialogFragment {

	private Player mPlayer;
	private EditText mTxtHost;
	private EditText mTxtPass;
	private Button mBtnRestore;

	public ConnectDialog() {

	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		super.onCreateDialog(savedInstanceState);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_connect, null);

		mPlayer = (Player) getActivity();

		final SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(mPlayer.getApplicationContext());

		mTxtHost = (EditText) view.findViewById(R.id.host_ip);
		mTxtHost.setText(sharedPref.getString(SettingsActivity.HOST,
				SettingsActivity.HOST_DEFAULT));

		mTxtPass = (EditText) view.findViewById(R.id.pass);
		mTxtPass.setText(sharedPref.getString(SettingsActivity.PASS,
				SettingsActivity.PASS_DEFAULT));

		mBtnRestore = (Button) view.findViewById(R.id.button_restore);
		mBtnRestore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				mTxtHost.setText(SettingsActivity.HOST_DEFAULT);
				mTxtPass.setText(SettingsActivity.PASS_DEFAULT);
			}
		});

		Dialog dialog = builder
				.setTitle(R.string.connect)
				.setView(view)
				.setPositiveButton(R.string.connect,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {

								// Save host and pass preferences
								SharedPreferences.Editor editor = sharedPref
										.edit();
								editor.putString(SettingsActivity.HOST,
										mTxtHost.getText().toString());
								editor.putString(SettingsActivity.PASS,
										mTxtPass.getText().toString());
								editor.commit();

								mPlayer.connect();
								dismiss();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int id) {

								getDialog().cancel();
							}
						}).create();

		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		return dialog;
	}
}
