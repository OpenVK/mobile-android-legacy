package uk.openvk.android.legacy.api.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.entities.Video;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Videos {
    private JSONParser jsonParser;
    private ArrayList<Video> videos;

    public Videos() {
        jsonParser = new JSONParser();
        videos = new ArrayList<Video>();
    }

    public void parse(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray videos = json.getJSONArray("response");
            if(this.videos.size() > 0) {
                this.videos.clear();
            }
            for (int i = 0; i < videos.length(); i++) {
                Video video = new Video(videos.getJSONObject(i));
                try {
                    this.videos.add(video);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    Log.e(OvkApplication.API_TAG, "WTF? The length itself in an array must not " +
                            "be overestimated.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getVideos(OvkAPIWrapper wrapper, long owner_id, int count) {
        wrapper.sendAPIMethod("Videos.get",
                String.format("owner_id=%s&count=%s", owner_id, count)
        );
    }

    public ArrayList<Video> getList() {
        return videos;
    }
}
