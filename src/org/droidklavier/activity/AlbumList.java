package org.droidklavier.activity;

import java.util.ArrayList;
import java.util.Locale;

import org.droidklavier.Droidklavier;
import org.droidklavier.R;
import org.droidklavier.db.Album;

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

public class AlbumList extends ListActivity {

	private Droidklavier mDroidklavier;
	private ArrayList<Album> mAlbumList;
	private Album mAlbum;
	private String mSource;
	private ProgressDialog mLoadingDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.album_list);

		String source = getIntent().getStringExtra(Player.SOURCE);
		String keyword = getIntent().getStringExtra(Player.KEYWORD);
		mDroidklavier = (Droidklavier) getApplication();

		setTitle(buildTitle(source, keyword));

		mLoadingDialog = new ProgressDialog(this);
		mLoadingDialog.setMessage(getString(R.string.loading));
		mLoadingDialog.show();

		new AlbumListTask().execute(source);

		getListView().setOnItemClickListener(
				new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> av, View v, int pos,
							long id) {

						startSongListActivity((int) id);
					}
				});
	}

	private CharSequence buildTitle(String source, String keyword) {

		String title;

		if (keyword != null && keyword.length() > 0) {
			title = String.format("Results for '%s'", keyword);
		} else {
			title = String.format("Albums from: %s",
					source.toUpperCase(Locale.getDefault()));
		}

		return title;
	}

	@Override
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);
	}

	private void startSongListActivity(int id) {

		mAlbum = mAlbumList.get(id);
		mSource = mAlbum.source;
		Intent intent = new Intent(this, SongList.class);
		intent.putExtra(Player.SOURCE, mSource);
		intent.putExtra(Player.ALBUM, mAlbum);
		startActivityForResult(intent, Player.SONG_LIST);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {

		case Player.SONG_LIST:
			if (resultCode == Activity.RESULT_OK) {
				Intent intent = new Intent();
				intent.putExtra(Player.SOURCE, mSource);
				intent.putExtra(Player.ALBUM, mAlbum);
				intent.putExtra(Player.SONG,
						data.getParcelableExtra(Player.SONG));
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
			break;
		}
	}

	private class AlbumListAdapter extends ArrayAdapter<Album> {

		private ArrayList<Album> items;

		public AlbumListAdapter(Context context, int textViewResourceId,
				ArrayList<Album> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.album_list_item, null);
			}
			Album album = items.get(position);
			if (album != null) {
				TextView album_title = (TextView) v
						.findViewById(R.id.album_title);
				TextView album_source = (TextView) v
						.findViewById(R.id.album_source);
				if (album_title != null) {
					album_title.setText(album.title);
				}
				if (album_source != null) {
					album_source.setText("Source: " + album.source);
				}
			}
			return v;
		}

	}

	private class AlbumListTask extends
			AsyncTask<String, Void, ArrayList<Album>> {

		@Override
		protected ArrayList<Album> doInBackground(String... params) {

			ArrayList<Album> albumList = mDroidklavier.getDAO().getAlbumList(
					params[0]);
			return albumList;
		}

		@Override
		protected void onPostExecute(ArrayList<Album> result) {

			super.onPostExecute(result);

			if (result != null) {
				mAlbumList = result;
				setListAdapter(new AlbumListAdapter(AlbumList.this,
						R.layout.album_list_item, mAlbumList));
			}

			if (mLoadingDialog != null) {
				mLoadingDialog.dismiss();
			}
		}

	}

}
