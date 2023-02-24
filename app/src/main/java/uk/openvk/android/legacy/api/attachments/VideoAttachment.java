package uk.openvk.android.legacy.api.attachments;

import android.os.Parcel;
import android.os.Parcelable;

import uk.openvk.android.legacy.api.models.VideoFiles;

/**
 * File created by Dmitry on 14.02.2023.
 */

public class VideoAttachment implements Parcelable {
    public VideoFiles files;
    public long id;
    public String title;
    public String url;
    public String url_thumb;
    public int duration;
    public String filename;

    public VideoAttachment() {

    }

    public VideoAttachment(long id, String title, VideoFiles files, String url_thumb, int duration, String filename) {
        this.id = id;
        this.title = title;
        this.files = files;
        this.url_thumb = url_thumb;
        this.duration = duration;
        this.filename = filename;
    }

    protected VideoAttachment(Parcel in) {
        id = in.readLong();
        title = in.readString();
        files = in.readParcelable(VideoFiles.class.getClassLoader());
        url_thumb = in.readString();
        duration = in.readInt();
        filename = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeParcelable(files, flags);
        dest.writeString(url_thumb);
        dest.writeInt(duration);
        dest.writeString(filename);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoAttachment> CREATOR = new Creator<VideoAttachment>() {
        @Override
        public VideoAttachment createFromParcel(Parcel in) {
            return new VideoAttachment(in);
        }

        @Override
        public VideoAttachment[] newArray(int size) {
            return new VideoAttachment[size];
        }
    };
}
