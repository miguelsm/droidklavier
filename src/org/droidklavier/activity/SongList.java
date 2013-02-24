package org.droidklavier.activity;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.droidklavier.Droidklavier;
import org.droidklavier.R;
import org.droidklavier.db.Album;
import org.droidklavier.db.DAO;
import org.droidklavier.db.Song;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SongList extends ListActivity {

	private Droidklavier mDroidklavier;
	private ArrayList<Song> mSongList;
	private Album mAlbum;
	private ProgressDialog mLoadingDialog;

	private final String regEx1 = "([^\\[]*)(\\[)(Pianosoft|User)(:)([^\\]]*)(\\])";
	private final String regEx2 = "(\\[)(Pianosoft|User)(:)";

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.song_list);

		mDroidklavier = (Droidklavier) getApplication();
		String source = getIntent().getStringExtra(Player.SOURCE);
		mAlbum = getIntent().getParcelableExtra(Player.ALBUM);

		if (mAlbum != null) {
			setTitle("Songs from: " + mAlbum.title);
		}

		mLoadingDialog = new ProgressDialog(this);
		mLoadingDialog.setMessage(getString(R.string.loading));
		mLoadingDialog.show();

		new SongListTask().execute(source);

		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> av, View v, int pos,
							long id) {

						selectSong((int) id);
					}
				});
	}

	private void selectSong(int id) {

		Song song = mSongList.get(id);
		String source = song.source;
		Intent intent = new Intent();
		intent.putExtra(Player.SOURCE, source);
		intent.putExtra(Player.ALBUM, mAlbum);
		intent.putExtra(Player.SONG, song);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	private class SongListAdapter extends ArrayAdapter<Song> {

		private ArrayList<Song> mmSongList;
		private String mmAlbumTitle;

		public SongListAdapter(Context context, int textViewResourceId,
				ArrayList<Song> songList, String albumTitle) {

			super(context, textViewResourceId, songList);
			mmSongList = songList;
			mmAlbumTitle = albumTitle;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.song_list_item, null);
			}
			Song song = mmSongList.get(position);
			if (song != null) {
				TextView songTitle = (TextView) v.findViewById(R.id.song_title);
				TextView songAlbum = (TextView) v.findViewById(R.id.song_album);
				TextView albumSource = (TextView) v
						.findViewById(R.id.album_source);
				String[] strings = match(song.title);

				if (strings != null && strings.length == 3) {
					songTitle.setText(strings[0]);
					songAlbum.setText("Album: " + strings[1]);
					albumSource.setText("Source: " + strings[2]);
				} else {
					songTitle.setText(song.title);
					songAlbum.setText("Album: " + mmAlbumTitle);
					albumSource.setText("Source: " + song.source);
				}
			}
			return v;
		}

	}

	private String[] match(final String songTitle) {

		String[] strings = null;
		Pattern p = Pattern.compile(regEx1);
		Matcher m = p.matcher(songTitle);

		if (m.find()) {
			strings = songTitle.split(regEx2);

			// strings = [song, album, source]
			strings = new String[] { strings[0],
					strings[1].substring(0, strings[1].length() - 1),
					m.group(3) };
		}

		return strings;
	}

	private class SongListTask extends AsyncTask<String, Void, ArrayList<Song>> {

		@Override
		protected ArrayList<Song> doInBackground(String... params) {

			ArrayList<Song> songList;
			String source = params[0];

			// Get the songs list from the database
			if (source.equals(DAO.SEARCH_RESULT)) {
				songList = mDroidklavier.getDAO().getSearchResultSongList(
						mAlbum.albumId);
			} else {
				songList = mDroidklavier.getDAO().getSongList(source,
						mAlbum.albumId);
			}

			return songList;
		}

		@Override
		protected void onPostExecute(ArrayList<Song> result) {

			super.onPostExecute(result);

			if (result != null) {
				mSongList = result;
				setListAdapter(new SongListAdapter(SongList.this,
						R.layout.song_list_item, mSongList, mAlbum.title));
			}

			if (mLoadingDialog != null) {
				mLoadingDialog.dismiss();
			}
		}

	}

}
