package org.droidklavier.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import org.droidklavier.R;
import org.droidklavier.activity.Player;

import java.util.Locale;

public class SearchDialog extends DialogFragment {

  private Player mPlayer;
  private EditText mTxtQuery;
  private CheckBox mChkLibraryPS;
  private CheckBox mChkLibraryU;
  private CheckBox[] mCheckBoxes;

  public SearchDialog() {

  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View view = inflater.inflate(R.layout.fragment_search, null);

    Dialog dialog = builder
      .setTitle(R.string.search)
      .setView(view)
      .setCancelable(false)
      .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          InputMethodManager imm = (InputMethodManager) mPlayer.getSystemService(Context.INPUT_METHOD_SERVICE);
          imm.hideSoftInputFromWindow(mTxtQuery.getWindowToken(), 0);
          if (search()) {
            dismiss();
          }
        }
      })
      .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
          getDialog().cancel();
        }
      })
      .create();

    mPlayer = (Player) getActivity();

    mChkLibraryPS = (CheckBox) view.findViewById(R.id.check_library_pianosoft);
    mChkLibraryU = (CheckBox) view.findViewById(R.id.check_library_user);
    mCheckBoxes = new CheckBox[] { mChkLibraryPS, mChkLibraryU };

    mTxtQuery = (EditText) view.findViewById(R.id.search_query);
    mTxtQuery.setOnEditorActionListener(new OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId,
        KeyEvent event) {
        InputMethodManager imm = (InputMethodManager) mPlayer.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        if (search()) {
          dismiss();
        }
        return false;
      }
    });

    return dialog;
  }

  private boolean search() {
    if (mPlayer == null) {
      return false;
    }
    if (mPlayer.isPlaying()) {
      mPlayer.infoMessage(R.string.stop_playing_first);
      return false;
    }

    String keyword = "";
    if (mTxtQuery != null) {
      keyword = mTxtQuery.getText().toString();
    }
    if (keyword.length() <= 0) {
      mPlayer.infoMessage(R.string.no_keyword);
      return false;
    }

    String sources = getSources(mCheckBoxes);
    if (sources.length() == 0) {
      mPlayer.infoMessage(R.string.no_library_selected);
      return false;
    }

    mPlayer.searchStart(sources, keyword);
    return true;
  }

  private static String getSources(final CheckBox[] checkboxes) {
    int numChecked = 0;
    String sources = "";

    for (int i = 0; i < checkboxes.length; i++) {
      if (checkboxes[i] != null && checkboxes[i].isChecked()) {
        numChecked++;
      }
    }

    for (int i = 0; i < numChecked; i++) {
      sources += checkboxes[i].getText();
      if (i < numChecked - 1) {
        sources += ",";
      }
    }

    return sources.toLowerCase(Locale.getDefault());
  }
}
