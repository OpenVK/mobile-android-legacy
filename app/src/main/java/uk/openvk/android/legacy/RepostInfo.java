package uk.openvk.android.legacy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

class RepostInfo {
    public String name;
    public String time;
    public NewsListItem nLI;
    public RepostInfo(String original_author, int dt_sec) {
        name = original_author;
        Date dt = new Date(TimeUnit.SECONDS.toMillis(dt_sec));
        String info = new SimpleDateFormat("dd MMMMM yyyy at HH:mm").format(dt);
    }
}
