package uk.openvk.android.legacy.list_items;

import android.graphics.Bitmap;

public class CommentsListItem {
    public String author;
    public String text;
    public String info;
    public Bitmap avatar;

    public CommentsListItem(String author, String text, Bitmap avatar) {
        this.author = author;
        this.text = text;
        this.avatar = avatar;
        this.info = info;
    }

    public Bitmap getAvatar() {
        return avatar;
    }
}
