package uk.openvk.android.legacy.api.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * File created by Dmitry on 24.02.2023.
 */

public class VideoFiles implements Parcelable {
    public String mp4_144;
    public String mp4_240;
    public String mp4_360;
    public String mp4_480;
    public String mp4_720;
    public String mp4_1080;
    public String ogv_480;

    public VideoFiles() {

    }

    protected VideoFiles(Parcel in) {
        mp4_144 = in.readString();
        mp4_240 = in.readString();
        mp4_360 = in.readString();
        mp4_480 = in.readString();
        mp4_720 = in.readString();
        mp4_1080 = in.readString();
        ogv_480 = in.readString();
    }

    public static final Creator<VideoFiles> CREATOR = new Creator<VideoFiles>() {
        @Override
        public VideoFiles createFromParcel(Parcel in) {
            return new VideoFiles(in);
        }

        @Override
        public VideoFiles[] newArray(int size) {
            return new VideoFiles[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mp4_144);
        dest.writeString(mp4_240);
        dest.writeString(mp4_360);
        dest.writeString(mp4_480);
        dest.writeString(mp4_720);
        dest.writeString(mp4_1080);
        dest.writeString(ogv_480);
    }
}
