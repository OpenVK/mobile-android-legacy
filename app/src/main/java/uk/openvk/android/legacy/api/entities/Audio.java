package uk.openvk.android.legacy.api.entities;


import android.os.Parcel;
import android.os.Parcelable;

public class Audio implements Parcelable {
    public String unique_id;
    public long id;
    public String artist;
    public String title;
    public String album;
    private int duration_sec;
    private String duration;
    public String genre;
    public boolean is_explicit;
    public String lyrics;
    public String url;
    public User sender;
    public int status;

    public Audio() {
    }

    protected Audio(Parcel in) {
        unique_id = in.readString();
        id = in.readLong();
        artist = in.readString();
        title = in.readString();
        album = in.readString();
        duration_sec = in.readInt();
        duration = in.readString();
        genre = in.readString();
        is_explicit = in.readByte() != 0;
        lyrics = in.readString();
        url = in.readString();
        status = in.readInt();
    }

    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

    public void setDuration(int duration_sec) {
        this.duration_sec = duration_sec;
        this.duration =
                String.format(
                        "%s:%s",
                        Math.floor((double)duration_sec / 60),
                        duration_sec % 60
                );
    }

    public String getDuration() {
        return duration;
    }

    public int getDurationInSeconds() {
        return duration_sec;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(unique_id);
        parcel.writeLong(id);
        parcel.writeString(artist);
        parcel.writeString(title);
        parcel.writeString(album);
        parcel.writeInt(duration_sec);
        parcel.writeString(duration);
        parcel.writeString(genre);
        parcel.writeByte((byte) (is_explicit ? 1 : 0));
        parcel.writeString(lyrics);
        parcel.writeString(url);
        parcel.writeInt(status);
    }
}
