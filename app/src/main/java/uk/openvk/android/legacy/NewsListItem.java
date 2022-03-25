package uk.openvk.android.legacy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NewsListItem {

    public String name;
    public RepostInfo repost;
    public String info;
    public String text;
    public NewsItemCountersInfo counters;
    public Bitmap avatar;
    public Bitmap photo;

    public NewsListItem(String author, int dt_sec, RepostInfo repostInfo, String post_text, NewsItemCountersInfo nICI, Bitmap author_avatar, Bitmap post_photo, Context ctx) {
        name = author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        info = new SimpleDateFormat("dd MMMM yyyy").format(dt) + " " + ctx.getResources().getString(R.string.date_at) + " " + new SimpleDateFormat("HH:mm:ss").format(dt);
        repost = repostInfo;
        counters = nICI;
        text = post_text;
        photo = post_photo;
        avatar = author_avatar;
    }
}
