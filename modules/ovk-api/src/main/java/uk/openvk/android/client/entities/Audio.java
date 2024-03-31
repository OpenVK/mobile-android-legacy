/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client.entities;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

import java.io.Serializable;

import uk.openvk.android.client.attachments.Attachment;

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
    public long lyrics;
    public String url;
    public User sender;
    public int status;
    public long owner_id;
    public String lyrics_text;

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
        lyrics = in.readLong();
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
        parcel.writeLong(lyrics);
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
            audio.put("lyrics", lyrics);
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
            lyrics = audio.getLong("lyrics");
            url = audio.getString("url");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
