package org.droidklavier.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.droidklavier.Droidklavier;
import org.droidklavier.R;
import org.droidklavier.db.Album;
import org.droidklavier.db.DAO;
import org.droidklavier.db.Song;
import org.droidklavier.dialog.BalanceDialog;
import org.droidklavier.dialog.ConnectDialog;
import org.droidklavier.dialog.MainVolumeDialog;
import org.droidklavier.dialog.SearchDialog;
import org.droidklavier.rc.RC;

import java.util.Locale;

public class Player extends FragmentActivity {

  // Intent request codes
  public static final int ALBUM_LIST = 0;
  public static final int SONG_LIST = 1;
  public static final int SEARCH = 2;
  public static final int SETTINGS = 3;

  // Intent Extras
  public static final String SOURCE = "org.droidklavier.gui.source";
  public static final String ALBUM = "org.droidklavier.gui.album";
  public static final String SONG = "org.droidklavier.gui.song";
  public static final String KEYWORD = "org.droidklavier.gui.keyword";
  public static final String SEARCH_RESULT = "org.droidklavier.gui.searchType";

  // Layout Views
  // private TextView mTitle;
  private TextView mTextViewAlbumTitle;
  private TextView mTextViewSongTitle;
  private TextView mTextSongTime;
  private TextView mTextSongLength;
  // private ProgressBar mTitleProgressBar;
  private SeekBar mSeekBarSongPosition;
  private Button mButtonLibrary;
  private Button mButtonSearch;
  private Button mButtonPlay;
  private Button mButtonPause;
  private Button mButtonPrev;
  private Button mButtonNext;
  private Button mButtonVolume;
  private Button mButtonBalance;
  private ToggleButton mButtonQuietStatus;
  private ToggleButton mButtonRcsStatus;
  private ProgressDialog mSearchingProgressDialog;
  private ProgressDialog mConnectingProgressDialog;
  private ProgressDialog mLoadingDialog;
  private Dialog mDialog;
  private DialogFragment mDialogFragment;

  private Droidklavier mDroidklavier;
  private String mSource;
  private Album mAlbum;
  private Song mSong;
  private String mKeyword;
  private boolean mLoading = false;
  private boolean mPlaying = false;
  private boolean mSeeking = false;
  private boolean mBrowsing = false;
  private boolean mSearching = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    mDroidklavier = (Droidklavier) getApplication();

    mButtonLibrary = (Button) findViewById(R.id.button_library);
    mButtonLibrary.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mLoading) {
          showLibraryDialog();
        }
      }
    });

    mButtonSearch = (Button) findViewById(R.id.button_search);
    mButtonSearch.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showSearchDialog();
      }
    });

    mTextViewAlbumTitle = (TextView) findViewById(R.id.text_album_title);
    mTextViewAlbumTitle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mLoading) {
          startAlbumListActivity(mSource);
        }
      }
    });

    mTextViewSongTitle = (TextView) findViewById(R.id.text_song_title);
    mTextViewSongTitle.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!mLoading) {
          startSongListActivity(mSource, mAlbum);
        }
      }
    });

    mSeekBarSongPosition = (SeekBar) findViewById(R.id.song_seek_bar);
    mSeekBarSongPosition.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (mLoading) {
          event.setAction(MotionEvent.ACTION_CANCEL);
        } else {
          mSeeking = true;
        }
        return false;
      }
    });

    mSeekBarSongPosition.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar,
        int progress, boolean fromUser) {
        mTextSongTime.setText(getSongTime(progress));
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        mDroidklavier.sendTCPMessage(RC
          .search(mSeekBarSongPosition.getProgress()));
      }
    });

    mSeekBarSongPosition.setIndeterminate(false);

    mButtonVolume = (Button) findViewById(R.id.button_volume);
    mButtonVolume.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showMainVolumeDialog();
      }
    });

    mButtonBalance = (Button) findViewById(R.id.button_balance);
    mButtonBalance.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showBalanceDialog();
      }
    });

    mTextSongLength = (TextView) findViewById(R.id.text_song_length);
    mTextSongTime = (TextView) findViewById(R.id.text_song_time);

    mButtonPlay = (Button) findViewById(R.id.button_play);
    mButtonPlay.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDroidklavier.sendTCPMessage(RC.play());
      }
    });

    mButtonPause = (Button) findViewById(R.id.button_pause);
    mButtonPause.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDroidklavier.sendTCPMessage(RC.pause());
      }
    });

    if (mButtonPlay.getVisibility() == Button.VISIBLE) {
      mButtonPause.setVisibility(Button.GONE);
    }

    mButtonPrev = (Button) findViewById(R.id.button_prev);
    mButtonPrev.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDroidklavier.sendTCPMessage(RC.prev());
      }
    });

    mButtonNext = (Button) findViewById(R.id.button_next);
    mButtonNext.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mDroidklavier.sendTCPMessage(RC.next());
      }
    });

    mButtonQuietStatus = (ToggleButton) findViewById(R.id.button_quiet);
    mButtonQuietStatus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String mode = (((ToggleButton) v).isChecked()) ?
          RC.QUIET_STATUS_QUIET : RC.QUIET_STATUS_ACOUSTIC;
        mDroidklavier.sendTCPMessage(RC.quietStatus(mode));
      }
    });

    mButtonRcsStatus = (ToggleButton) findViewById(R.id.button_standby);
    mButtonRcsStatus.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (((ToggleButton) v).isChecked()) {
          mDroidklavier.sendTCPMessage(RC.setRcsStatus("on"));
        } else {
          mDroidklavier.sendTCPMessage(RC.setRcsStatus("standby"));
        }
      }
    });

    mSearchingProgressDialog = new ProgressDialog(this);
    mSearchingProgressDialog.setTitle(getString(R.string.search));
    mSearchingProgressDialog.setMessage(getString(R.string.searching));
    mSearchingProgressDialog.setCancelable(false);
    mSearchingProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
        getString(R.string.stop),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mDroidklavier.sendTCPMessage(RC.fileFuncCancel(""));
          }
        });

    mConnectingProgressDialog = new ProgressDialog(this);
    mConnectingProgressDialog.setTitle(getString(R.string.connect));
    mConnectingProgressDialog.setMessage(getString(R.string.connecting));
    mConnectingProgressDialog.setCancelable(false);
    mConnectingProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
        getString(R.string.stop),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        });

    mLoadingDialog = new ProgressDialog(this);
    mLoadingDialog.setMessage(getString(R.string.loading));

    if (mDroidklavier.isConnected()) {
      setActionBarStatus(getString(R.string.connected) + mDroidklavier.getConnectedAddr());
    } else {
      setActionBarStatus(getString(R.string.disconnected));
    }
  }

  public void setActionBarStatus(String status) {
    getActionBar().setSubtitle(status);
  }

  @Override
  public void onStart() {
    super.onStart();
    mDroidklavier.setPlayer(this);

    if (!mDroidklavier.isConnected()) {
      connect();
    } else {
      mDroidklavier.sendTCPMessage(RC.rcStatus());
      mDroidklavier.sendTCPMessage(RC.getRcsStatus());
      mDroidklavier.sendTCPMessage(RC.quietStatus());
    }

    statusLoading(mDroidklavier.isLoading());
    mBrowsing = false;
    dismissDialogs();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mDroidklavier.setPlayer(null);
    mDroidklavier.disconnect();
  }

  public void connect() {
    dismissDialogs();
    mDialog = mConnectingProgressDialog;
    mDialog.show();
    mDroidklavier.reconnect();
  }

  public void connectionSuccess() {
    dismissDialogs();
    String connectedAddr = mDroidklavier.getConnectedAddr();
    setActionBarStatus(String.format(getString(R.string.connected), connectedAddr));
    infoMessage(String.format(getString(R.string.connection_success), connectedAddr));
    mDroidklavier.sendTCPMessage(RC.rcStatus());
    mDroidklavier.sendTCPMessage(RC.getRcsStatus());
    mDroidklavier.sendTCPMessage(RC.quietStatus());
  }

  public void connectionFailed() {
    infoMessage(R.string.connection_failed);
    showConnectDialog();
  }

  public void connectionLost() {
    setActionBarStatus(getString(R.string.disconnected));
  }

  public boolean isPianoOn() {
    return mButtonRcsStatus != null && mButtonRcsStatus.isChecked();
  }

  public boolean isPlaying() {
    return mPlaying;
  }

  public boolean isSearching() {
    return mSearching;
  }

  public void infoMessage(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  public void infoMessage(int string) {
    Toast.makeText(this, getString(string), Toast.LENGTH_SHORT).show();
  }

  public void updateRCSStatus(String status) {
    if (status.equals("on")) {
      mButtonRcsStatus.setChecked(true);
    } else if (status.equals("standby")) {
      mButtonRcsStatus.setChecked(false);
    }
  }

  public void updateQuietStatus(String mode) {
    if (mode.equals(RC.QUIET_STATUS_QUIET)) {
      mButtonQuietStatus.setChecked(true);
    } else if (mode.equals(RC.QUIET_STATUS_ACOUSTIC)) {
      mButtonQuietStatus.setChecked(false);
    }
  }

  public void updateSeekingStatus(boolean seeking) {
    mSeeking = seeking;
  }

  public void updateSeqStatus(String status, String source, int albumId,
      int selSongNo, int time) {

    if (mSource == null || mAlbum == null || mSong == null) {
      updatePlaybackInfo(source, albumId, selSongNo);
    }

    if (status.equals("play")) {

      if (!mPlaying) {
        mButtonPlay.setVisibility(Button.GONE);
        mButtonPause.setVisibility(Button.VISIBLE);
        mPlaying = true;
      }

      if (!mSeeking && !mLoading) {
        mSeekBarSongPosition.setProgress(time);
        mTextSongTime.setText(getSongTime(time));
      }

      if (mLoading) {
        updatePlaybackInfo(source, albumId, selSongNo);
        statusLoading(false);
      }

    } else if (status.equals("pause") || status.equals("stop")) {

      if (mPlaying) {
        mButtonPlay.setVisibility(Button.VISIBLE);
        mButtonPause.setVisibility(Button.GONE);
        mPlaying = false;
      }

      if (!mSeeking && !mLoading) {
        mSeekBarSongPosition.setProgress(time);
        mTextSongTime.setText(getSongTime(time));
      }

      if (mLoading) {
        updatePlaybackInfo(source, albumId, selSongNo);
        statusLoading(false);
      }

    } else if (status.equals("load")) {
      statusLoading(true);
    } else if (status.equals("loaded")) {
      updatePlaybackInfo(source, albumId, selSongNo);
      statusLoading(false);
    }
  }

  private void updatePlaybackInfo(String source, int albumId, int selSongNo) {
    if (checkSource(source)) {
      mSource = source;
      new UpdateAlbumTask().execute(albumId);
      new UpdateSongTask().execute(albumId, selSongNo);
    }
  }

  private void statusLoading(boolean loading) {

    if (loading) {
      mDialog = mLoadingDialog;
      mDialog.show();
      mSeekBarSongPosition.setIndeterminate(true);
    } else {
      mLoadingDialog.dismiss();
      mSeekBarSongPosition.setIndeterminate(false);
    }

    mLoading = loading;
    mDroidklavier.setLoading(loading);
  }

  private boolean checkSource(String source) {
    if (source != null) {
      String checkSource = source.toLowerCase(Locale.getDefault());
      return checkSource.indexOf(DAO.PIANOSOFT) > -1
        || checkSource.indexOf(DAO.USER) > -1
        || checkSource.indexOf(DAO.SEARCH_RESULT) > -1;
    }
    return false;
  }

  private String getSongTime(int millis) {
    int seconds = (millis / 1000);
    int minutes = seconds / 60;
    seconds -= minutes * 60;
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
  }

  public void searchStart(String sourceId, String keyword) {
    mSearching = true;
    mKeyword = keyword;
    mDroidklavier.sendTCPMessage(RC.songSearch(sourceId, mKeyword));
    mDialog = mSearchingProgressDialog;
    mDialog.show();
  }

  public void searchProcessing(String message) {
    if (mSearching && mSearchingProgressDialog != null) {
      mSearchingProgressDialog.setMessage(message);
    }
  }

  public void searchEnd(String title, String message) {
    if (mSearching && mSearchingProgressDialog != null) {
      mSearching = false;
      mSearchingProgressDialog.dismiss();
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(getString(R.string.ok),
            new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog,
                int id) {
                startAlbumListActivity(DAO.SEARCH_RESULT);
              }
            })
        .setNegativeButton(getString(R.string.cancel),
          new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,
              int id) {
              dialog.dismiss();
            }
          }).create().show();
    }
  }

  /**
   * DIALOGS
   *
   */

  protected void showConnectDialog() {
    dismissDialogs();
    FragmentManager fm = getSupportFragmentManager();
    mDialogFragment = new ConnectDialog();
    mDialogFragment.show(fm, "fragment_connect");
  }

  protected void showSearchDialog() {
    dismissDialogs();
    FragmentManager fm = getSupportFragmentManager();
    mDialogFragment = new SearchDialog();
    mDialogFragment.show(fm, "fragment_search");
  }

  protected void showLibraryDialog() {
    final String[] libraries = getResources().getStringArray(
        R.array.libraries);
    dismissDialogs();
    mDialog = new AlertDialog.Builder(this).setTitle(R.string.open_library)
      .setItems(libraries, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface d, int choice) {
          startAlbumListActivity(libraries[choice]);
        }
      }).create();
    mDialog.show();
  }

  protected void showMainVolumeDialog() {
    dismissDialogs();
    FragmentManager fm = getSupportFragmentManager();
    mDialogFragment = new MainVolumeDialog();
    mDialogFragment.show(fm, "fragment_main_volume");
  }

  protected void showBalanceDialog() {
    dismissDialogs();
    FragmentManager fm = getSupportFragmentManager();
    mDialogFragment = new BalanceDialog();
    mDialogFragment.show(fm, "fragment_balance");
  }

  private void dismissDialogs() {
    if (mDialog != null) {
      mDialog.dismiss();
    }
    if (mDialogFragment != null) {
      mDialogFragment.dismiss();
    }
  }

  /**
   * ACTIVITIES
   *
   */

  private void startAlbumListActivity(String source) {
    if (mDroidklavier.isConnected() && checkSource(source) && !mBrowsing) {
      mBrowsing = true;
      Intent intent = new Intent(this, AlbumList.class);
      intent.putExtra(SOURCE, source.toLowerCase(Locale.getDefault()));
      intent.putExtra(Player.KEYWORD, mKeyword);
      startActivityForResult(intent, ALBUM_LIST);
    }
  }

  private void startSongListActivity(String source, Album album) {
    if (mDroidklavier.isConnected() && checkSource(source) && !mBrowsing) {
      mBrowsing = true;
      Intent intent = new Intent(this, SongList.class);
      intent.putExtra(Player.SOURCE, source.toLowerCase(Locale.getDefault()));
      intent.putExtra(Player.ALBUM, album);
      startActivityForResult(intent, SONG_LIST);
    }
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case ALBUM_LIST:
      case SONG_LIST:
        if (resultCode == Activity.RESULT_OK) {
          if (!isPianoOn()) {
            Toast.makeText(this, R.string.turn_piano_on,
                Toast.LENGTH_SHORT).show();
          } else {
            mSource = data.getStringExtra(SOURCE);
            mAlbum = data.getParcelableExtra(ALBUM);
            mSong = data.getParcelableExtra(SONG);
            mDroidklavier.sendTCPMessage(RC.loadSongSource(mSource,
                  mAlbum.albumId, mSong.displayOrder));
            statusLoading(true);
          }
        }
        mBrowsing = false;
        break;
    }
  }

  /**
   * OPTIONS MENU
   *
   */

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.option_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.reconnect:
        showConnectDialog();
        return true;

        // case R.id.settings:
        // Intent intentSettings = new Intent(this, SettingsActivity.class);
        // startActivityForResult(intentSettings, SETTINGS);
        // return true;
    }
    return false;
  }

  /**
   * ASYNCTASKS
   *
   */

  private class UpdateAlbumTask extends AsyncTask<Integer, Void, Album> {

    @Override
    protected Album doInBackground(Integer... params) {
      int albumId = (Integer) params[0];
      Album album = mDroidklavier.getDAO().getAlbum(mSource, albumId);
      return album;
    }

    @Override
    protected void onPostExecute(Album result) {
      super.onPostExecute(result);
      if (result != null) {
        mAlbum = result;
        mTextViewAlbumTitle.setText(mAlbum.title);
      }
    }

  }

  private class UpdateSongTask extends AsyncTask<Integer, Void, Song> {

    @Override
    protected Song doInBackground(Integer... params) {
      Song song;
      int albumId = (Integer) params[0];
      int selSongNo = (Integer) params[1];
      if (mSource.equals(DAO.SEARCH_RESULT)) {
        song = mDroidklavier.getDAO().getSearchResultSong(selSongNo, albumId);
      } else {
        song = mDroidklavier.getDAO().getSong(mSource, albumId, selSongNo);
      }
      return song;
    }

    @Override
    protected void onPostExecute(Song result) {
      super.onPostExecute(result);
      if (result != null) {
        mSong = result;
        mTextViewSongTitle.setText(mSong.title);
        mTextSongLength.setText(getSongTime(mSong.length));
        mSeekBarSongPosition.setMax(mSong.length);
      }
    }
  }
}
