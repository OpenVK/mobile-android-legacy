package uk.openvk.android.legacy.list_items;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;

public class NewsfeedItem implements Parcelable {

    private String avatar_url;
    public String photo_msize_url;
    public String photo_hsize_url;
    public Bitmap avatar;
    public Bitmap photo;
    public String name;
    public RepostInfo repost;
    public String info;
    public String text;
    public int owner_id;
    public int post_id;
    public String photo_status;
    public NewsItemCountersInfo counters;
    public int author_id;

    public NewsfeedItem(String author, int dt_sec, RepostInfo repostInfo, String post_text, NewsItemCountersInfo nICI, String avatar_url, String photo_msize_url,
                        String photo_hsize_url, int o_id, int p_id, Context ctx) {
        name = author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        if((System.currentTimeMillis() - (dt_sec * 1000)) < 86400000) {
            info = ctx.getResources().getString(R.string.today_at) + " " + new SimpleDateFormat("HH:mm").format(dt);
        } if((System.currentTimeMillis() - (dt_sec * 1000)) < (86400000 * 2)) {
            info = ctx.getResources().getString(R.string.yesterday_at) + " " + new SimpleDateFormat("HH:mm").format(dt);
        } else {
            info = new SimpleDateFormat("d MMMM yyyy").format(dt) + " " + ctx.getResources().getString(R.string.date_at) + " " + new SimpleDateFormat("HH:mm").format(dt);
        }
        repost = repostInfo;
        counters = nICI;
        text = post_text;
        this.avatar_url = avatar_url;
        this.photo_msize_url = photo_msize_url;
        this.photo_hsize_url = photo_hsize_url;
        owner_id = o_id;
        post_id = p_id;
        if(photo_msize_url.length() > 0 || photo_hsize_url.length() > 0) {
            this.photo_status = "loading";
        } else {
            this.photo_status = "none";
        }
    }

    protected NewsfeedItem(Parcel in) {
        avatar_url = in.readString();
        photo_msize_url = in.readString();
        photo_hsize_url = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        photo = in.readParcelable(Bitmap.class.getClassLoader());
        name = in.readString();
        info = in.readString();
        text = in.readString();
        owner_id = in.readInt();
        post_id = in.readInt();
        photo_status = in.readString();
        author_id = in.readInt();
    }

    public static final Creator<NewsfeedItem> CREATOR = new Creator<NewsfeedItem>() {
        @Override
        public NewsfeedItem createFromParcel(Parcel in) {
            return new NewsfeedItem(in);
        }

        @Override
        public NewsfeedItem[] newArray(int size) {
            return new NewsfeedItem[size];
        }
    };

    public NewsfeedItem() {
        counters = new NewsItemCountersInfo();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(avatar_url);
        parcel.writeString(photo_msize_url);
        parcel.writeString(photo_hsize_url);
        parcel.writeParcelable(avatar, i);
        parcel.writeParcelable(photo, i);
        parcel.writeString(name);
        parcel.writeString(info);
        parcel.writeString(text);
        parcel.writeInt(owner_id);
        parcel.writeInt(post_id);
        parcel.writeString(photo_status);
        parcel.writeInt(author_id);
    }
}
