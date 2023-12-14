package uk.openvk.android.legacy.api.entities;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.Serializable;

import uk.openvk.android.legacy.api.attachments.Attachment;

public class Audio extends Attachment implements Parcelable, Serializable {
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
    public long owner_id;

    public Audio() {
        type = "audio";
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

    @SuppressLint("DefaultLocale")
    @SuppressWarnings("MalformedFormatString")
    public void setDuration(int duration_sec) {
        this.duration_sec = duration_sec;
        this.duration =
                String.format(
                        "%s:%02d",
                        (int) Math.floor(duration_sec / 60),
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

    @Override
    public void serialize(JSONObject object) {
        super.serialize(object);
        try {
            JSONObject audio = new JSONObject();
            audio.put("id", id);
            audio.put("unique_id", unique_id);
            audio.put("owner_id", owner_id);
            audio.put("artist", artist);
            audio.put("title", title);
            audio.put("album", album);
            audio.put("duration", duration_sec);
            audio.put("is_explicit", is_explicit);
            audio.put("url", url);
            object.put("audio", audio);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deserialize(String attach_blob) {
        try {
            super.deserialize(attach_blob);
            JSONObject audio = unserialized_data.getJSONObject("audio");
            id = audio.getLong("id");
            unique_id = audio.getString("unique_id");
            owner_id = audio.getLong("owner_id");
            artist = audio.getString("artist");
            title = audio.getString("title");
            duration_sec = audio.getInt("duration");
            this.duration =
                    String.format(
                            "%s:%s",
                            Math.floor((double)duration_sec / 60),
                            duration_sec % 60
                    );
            is_explicit = audio.getBoolean("is_explicit");
            url = audio.getString("url");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
