package uk.openvk.android.legacy.api.attachments;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;


public class PhotoAttachment implements Parcelable {
    public long id;
    public String url;
    public String original_url;
    public String filename;
    public Bitmap photo;
    public PhotoAttachment() {
    }

    protected PhotoAttachment(Parcel in) {
        url = in.readString();
        filename = in.readString();
    }

    public static final Creator<PhotoAttachment> CREATOR = new Creator<PhotoAttachment>() {
        @Override
        public PhotoAttachment createFromParcel(Parcel in) {
            return new PhotoAttachment(in);
        }

        @Override
        public PhotoAttachment[] newArray(int size) {
            return new PhotoAttachment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(filename);
    }
}
