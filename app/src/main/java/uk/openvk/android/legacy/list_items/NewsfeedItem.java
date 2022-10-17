package uk.openvk.android.legacy.list_items;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.counters.PostCounters;
import uk.openvk.android.legacy.api.models.Poll;

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
    public String attachment_status;
    public PostCounters counters;
    public int author_id;
    public Poll poll;

    public NewsfeedItem(String author, int dt_sec, RepostInfo repostInfo, String post_text, PostCounters nICI, String avatar_url, String photo_msize_url,
                        String photo_hsize_url, Poll poll, int o_id, int p_id, Context ctx) {
        name = author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        Date dt_midnight = new Date(System.currentTimeMillis() + 86400000);
        dt_midnight.setHours(0);
        dt_midnight.setMinutes(0);
        dt_midnight.setSeconds(0);
        if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 86400000) {
            info = String.format("%s %s", ctx.getResources().getString(R.string.today_at), new SimpleDateFormat("HH:mm").format(dt));
        } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < (86400000 * 2)) {
            info = String.format("%s %s", ctx.getResources().getString(R.string.yesterday_at), new SimpleDateFormat("HH:mm").format(dt));
        } else if((dt_midnight.getTime() - (TimeUnit.SECONDS.toMillis(dt_sec))) < 31536000000L) {
            info = String.format("%s %s %s", new SimpleDateFormat("d MMMM").format(dt), ctx.getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt));
        } else {
            info = String.format("%s %s %s", new SimpleDateFormat("d MMMM yyyy").format(dt), ctx.getResources().getString(R.string.date_at), new SimpleDateFormat("HH:mm").format(dt));
        }
        repost = repostInfo;
        counters = nICI;
        text = post_text;
        this.avatar_url = avatar_url;
        this.photo_msize_url = photo_msize_url;
        this.photo_hsize_url = photo_hsize_url;
        owner_id = o_id;
        post_id = p_id;
        this.poll = poll;
    }

    public NewsfeedItem() {

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
        attachment_status = in.readString();
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
        parcel.writeString(attachment_status);
        parcel.writeInt(author_id);
    }
}
