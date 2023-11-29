package uk.openvk.android.legacy.api.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;
import uk.openvk.android.legacy.api.entities.WallPost;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class Newsfeed implements Parcelable {
    private JSONParser jsonParser;
    private ArrayList<WallPost> items;
    private ArrayList<Photo> photos_lsize;
    private ArrayList<Photo> photos_msize;
    private ArrayList<Photo> photos_hsize;
    private ArrayList<Photo> photos_osize;
    private ArrayList<Photo> video_thumbnails;

    public long next_from;
    private DownloadManager dlm;

    public Newsfeed(String response, DownloadManager downloadManager, String quality, Context ctx) {
        jsonParser = new JSONParser();
        if(items == null) {
            parse(ctx, downloadManager, response, quality, true);
        }
    }

    public Newsfeed() {
        jsonParser = new JSONParser();
    }

    protected Newsfeed(Parcel in) {
        items = in.createTypedArrayList(WallPost.CREATOR);
    }

    public static final Creator<Newsfeed> CREATOR = new Creator<Newsfeed>() {
        @Override
        public Newsfeed createFromParcel(Parcel in) {
            return new Newsfeed(in);
        }

        @Override
        public Newsfeed[] newArray(int size) {
            return new Newsfeed[size];
        }
    };

    public void parse(Context ctx, DownloadManager downloadManager, String response, String quality, boolean clear) {
        this.dlm = downloadManager;
        if(clear) {
            items = new ArrayList<>();
        }
        Wall wall = new Wall();
        wall.setWallItems(items);
        wall.parse(ctx, downloadManager, quality, response, clear, false);
        items = wall.getWallItems();
    }

    public void get(OvkAPIWrapper wrapper, int count) {
        wrapper.sendAPIMethod("Newsfeed.get", String.format("count=%s&extended=1", count));
    }

    public void get(OvkAPIWrapper wrapper, int count, long start_from) {
        wrapper.sendAPIMethod("Newsfeed.get", String.format("count=%s&start_from=%s&extended=1", count, start_from), "more_news");
    }

    public void getGlobal(OvkAPIWrapper wrapper, int count) {
        wrapper.sendAPIMethod("Newsfeed.getGlobal", String.format("count=%s&extended=1", count));
    }

    public void getGlobal(OvkAPIWrapper wrapper, int count, long start_from) {
        wrapper.sendAPIMethod("Newsfeed.getGlobal", String.format("count=%s&start_from=%s&extended=1", count, start_from), "more_news");
    }

    public ArrayList<WallPost> getWallPosts() {
        return items;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(items);
    }

}
