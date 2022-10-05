package uk.openvk.android.legacy.list_items;

import android.content.Context;
import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;

public class NewsfeedItem {

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

}
