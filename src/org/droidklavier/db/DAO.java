package org.droidklavier.db;

import java.sql.ResultSet;
import java.util.ArrayList;

import android.annotation.SuppressLint;

/**
 * Data Access Object
 * 
 */
@SuppressLint("DefaultLocale")
public class DAO {

	public static final String PIANOSOFT = "pianosoft";
	public static final String USER = "user";
	public static final String SEARCH_RESULT = "search_result";

	private static final String ALBUM_COLUMN_NAMES = "album_id, title";
	private static final String SONG_COLUMN_NAMES = "song_id, display_order, album_id, title, length";

	private DBClient mDBClient;

	public DAO(DBClient dbClient) {

		mDBClient = dbClient;
	}

	public void setDBClient(DBClient dbClient) {

		mDBClient = dbClient;
	}

	public void setHost(String host){
		if(mDBClient != null)
			mDBClient.setHost(host);
	}
	
	public Album getAlbum(String source, int albumId) {

		Album album = null;
		try {

			String query = String.format(
					"SELECT %1$s FROM %2$s_album WHERE album_id=%3$d",
					ALBUM_COLUMN_NAMES, source, albumId);
			ResultSet resultset = mDBClient.executeQuery(query);

			while (resultset.next()) {
				album = new Album(source, resultset.getInt("album_id"),
						resultset.getString("title"));
			}

			resultset.close();

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			mDBClient.disconnect();
		}

		return album;
	}

	public Song getSong(String source, int albumId, int displayOrder) {

		Song song = null;
		try {

			String query = String
					.format("SELECT song_id, display_order, album_id, title, length FROM %1$s_song WHERE album_id = %2$d AND display_order = %3$d",
							source, albumId, displayOrder);
			ResultSet rs = mDBClient.executeQuery(query);

			while (rs.next())
				song = new Song(source, rs.getInt("song_id"),
						rs.getInt("display_order"), rs.getInt("album_id"),
						rs.getString("title"), rs.getInt("length"));

			rs.close();

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			mDBClient.disconnect();
		}

		return song;
	}

	public Song getSearchResultSong(int songId, int albumId) {

		Song song = null;
		try {

			String query = String
					.format("SELECT source FROM search_result_song WHERE song_id=%1$d AND album_id=%2$d",
							songId, albumId);
			ResultSet rs = mDBClient.executeQuery(query);

			String source = "";
			while (rs.next()) {
				source = rs.getString("source");
			}

			query = String
					.format("SELECT %1$s_song.song_id, %1$s_song.display_order, %1$s_song.album_id, %1$s_song.title, %1$s_song.length FROM %1$s_song, search_result_song"
							+ " WHERE search_result_song.album_id=%2$d AND search_result_song.song_id =%3$d AND search_result_song.song_id_to_play = %1$s_song.song_id",
							source, albumId, songId);
			rs = mDBClient.executeQuery(query);

			while (rs.next()) {
				song = new Song(source, rs.getInt("song_id"),
						rs.getInt("display_order"), rs.getInt("album_id"),
						rs.getString("title"), rs.getInt("length"));
			}

			rs.close();

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			mDBClient.disconnect();
		}

		return song;
	}

	public ArrayList<Album> getAlbumList(String source) {

		ArrayList<Album> albums = new ArrayList<Album>();
		try {

			String query = String.format(
					"SELECT %1$s FROM %2$s_album ORDER BY display_order",
					ALBUM_COLUMN_NAMES, source);
			ResultSet rs = mDBClient.executeQuery(query);

			while (rs.next()) {
				albums.add(new Album(source, rs.getInt("album_id"), rs
						.getString("title")));
			}

			rs.close();

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			mDBClient.disconnect();
		}

		return albums;
	}

	public ArrayList<Song> getSongList(String source, int albumId) {

		ArrayList<Song> songList = new ArrayList<Song>();
		try {

			String query = String
					.format("SELECT %1$s FROM %2$s_song WHERE album_id=%3$d ORDER BY display_order",
							SONG_COLUMN_NAMES, source, albumId);
			ResultSet rs = mDBClient.executeQuery(query);

			while (rs.next()) {
				songList.add(new Song(source, rs.getInt("song_id"), rs
						.getInt("display_order"), rs.getInt("album_id"), rs
						.getString("title"), rs.getInt("length")));
			}

			rs.close();

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			mDBClient.disconnect();
		}

		return songList;
	}

	public ArrayList<Song> getSearchResultSongList(int albumId) {

		ArrayList<Song> songs = new ArrayList<Song>();
		try {

			String query = String
					.format("SELECT * FROM search_result_song WHERE album_id=%d ORDER BY display_order",
							albumId);
			ResultSet rs = mDBClient.executeQuery(query);

			while (rs.next()) {
				songs.add(new Song(SEARCH_RESULT, rs.getInt("song_id"), rs
						.getInt("display_order"), rs.getInt("album_id"), rs
						.getString("title"), 0));
			}

			rs.close();

		} catch (Exception e) {

			e.printStackTrace();
		} finally {

			mDBClient.disconnect();
		}

		return songs;
	}
}
