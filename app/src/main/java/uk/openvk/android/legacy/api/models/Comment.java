package uk.openvk.android.legacy.api.models;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

/**
 * Created by Dmitry on 04.10.2022.
 */

public class Comment {
    public String author;
    public int date;
    public String text;
    private JSONParser jsonParser;

    public Comment() {
        jsonParser = new JSONParser();
    }

    public Comment(String author, int date, String text) {
        this.author = author;
        this.date = date;
        this.text = text;
    }
}
