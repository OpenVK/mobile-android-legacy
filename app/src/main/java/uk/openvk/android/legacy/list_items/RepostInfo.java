package uk.openvk.android.legacy.list_items;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.list_items.NewsfeedItem;

public class RepostInfo {
    public String name;
    public String time;
    public NewsfeedItem nLI;
    public RepostInfo(String original_author, int dt_sec) {
        name = original_author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        String info = new SimpleDateFormat("dd MMMMM yyyy at HH:mm").format(dt);
    }
}
