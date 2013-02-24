package org.droidklavier.db;

import android.os.Parcel;
import android.os.Parcelable;

public class Album implements Parcelable {

	public final String source;
	public final int albumId;
	public final String title;

	public Album(String source, int albumId, String title) {

		this.source = source;
		this.albumId = albumId;
		this.title = title;
	}

	public Album(Parcel in) {

		source = in.readString();
		albumId = in.readInt();
		title = in.readString();
	}

	@Override
	public int describeContents() {

		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(source);
		dest.writeInt(albumId);
		dest.writeString(title);
	}

	public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {

		public Album createFromParcel(Parcel in) {
			return new Album(in);
		}

		public Album[] newArray(int size) {
			return new Album[size];
		}
	};

}
