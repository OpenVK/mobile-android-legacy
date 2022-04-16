package uk.openvk.android.legacy.list_items;

import android.graphics.Bitmap;

public class FriendsListItem {
    public int id;
    public String name;
    public Bitmap avatar;
    public int online;

    public FriendsListItem(int user_id, String author, Bitmap author_avatar, int user_online) {
        id = user_id;
        name = author;
        avatar = author_avatar;
        online = user_online;
    }
}
