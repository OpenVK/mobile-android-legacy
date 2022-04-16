package uk.openvk.android.legacy.items;

import android.graphics.Bitmap;

public class SearchResultItem {
    public int id;
    public String title;
    public String subtitle;
    public Bitmap avatar;
    public int online;

    public SearchResultItem(int ovk_id, String item_title, String item_subtitle, Bitmap author_avatar, int user_online) {
        id = ovk_id;
        title = item_title;
        subtitle = item_subtitle;
        avatar = author_avatar;
        if(ovk_id < 0) {
            online = 0;
        } else {
            online = user_online;
        }
    }
}
