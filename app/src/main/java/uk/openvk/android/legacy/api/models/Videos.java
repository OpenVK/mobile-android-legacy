package uk.openvk.android.legacy.api.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.entities.Video;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Videos {
    private JSONParser jsonParser;
    private ArrayList<Video> videos;

    public Videos() {
        jsonParser = new JSONParser();
        videos = new ArrayList<Video>();
    }

    public void parse(DownloadManager dlman, String response) {
        try {
            ArrayList<Photo> video_thumbnails = new ArrayList<>();
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray videos = json.getJSONObject("response").getJSONArray("items");
            if(this.videos.size() > 0) {
                this.videos.clear();
            }
            for (int i = 0; i < videos.length(); i++) {
                JSONObject video_obj = videos.getJSONObject(i).getJSONObject("video");
                Video video = new Video(video_obj);
                try {
                    if (video_obj.has("image")) {
                        JSONArray thumb_array = video_obj.getJSONArray("image");
                        video.url_thumb = thumb_array.getJSONObject(0).getString("url");
                        Photo thumbnail = new Photo();
                        thumbnail.url = video.url_thumb;
                        thumbnail.filename = String.format("thumbnail_%so%s",
                                video.id, video.owner_id);
                        video_thumbnails.add(thumbnail);
                    }
                    this.videos.add(video);
                } catch (ArrayIndexOutOfBoundsException ignored) {
                    Log.e(OvkApplication.API_TAG, "WTF? The length itself in an array must not " +
                            "be overestimated.");
                }
            }
            dlman.downloadPhotosToCache(video_thumbnails, "video_thumbnails");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getVideos(OvkAPIWrapper wrapper, long owner_id, int count) {
        wrapper.sendAPIMethod("Video.get",
                String.format("owner_id=%s&count=%s", owner_id, count)
        );
    }

    public ArrayList<Video> getList() {
        return videos;
    }
}
