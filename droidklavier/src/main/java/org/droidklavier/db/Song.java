package org.droidklavier.db;

import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {

  public final String source;
  public final int songId;
  public final int displayOrder;
  public final int albumId;
  public final String title;
  public final int length;

  public Song(String source, int songId, int displayOrder, int albumId,
      String title, int length) {

    this.source = source;
    this.songId = songId;
    this.displayOrder = displayOrder;
    this.albumId = albumId;
    this.title = title;
    this.length = length;
  }

  public Song(Parcel in) {
    source = in.readString();
    songId = in.readInt();
    displayOrder = in.readInt();
    albumId = in.readInt();
    title = in.readString();
    length = in.readInt();
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(source);
    dest.writeInt(songId);
    dest.writeInt(displayOrder);
    dest.writeInt(albumId);
    dest.writeString(title);
    dest.writeInt(length);
  }

  public static final Parcelable.Creator<Song> CREATOR = new Parcelable.Creator<Song>() {

    public Song createFromParcel(Parcel in) {
      return new Song(in);
    }

    public Song[] newArray(int size) {
      return new Song[size];
    }
  };
}
