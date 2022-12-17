package uk.openvk.android.legacy.api.models;

import android.graphics.Bitmap;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

/**
 * Created by Dmitry on 04.10.2022.
 */

public class Comment {
    public String author;
    public long author_id;
    public long date;
    public String text;
    public long id;
    public Bitmap avatar;
    public String avatar_url;
    private JSONParser jsonParser;

    public Comment() {
        jsonParser = new JSONParser();
    }

    public Comment(int id, long author_id, String author, int date, String text) {
        this.author_id = author_id;
        this.author = author;
        this.date = date;
        this.text = text;
        this.id = id;
    }
}
