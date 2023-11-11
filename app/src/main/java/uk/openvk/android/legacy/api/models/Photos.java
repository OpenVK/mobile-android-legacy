package uk.openvk.android.legacy.api.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.entities.PhotoAlbum;
import uk.openvk.android.legacy.api.wrappers.DownloadManager;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

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

public class Photos {
    private JSONParser jsonParser;
    public String wallUploadServer;
    public String ownerPhotoUploadServer;
    public ArrayList<Photo> list;
    public ArrayList<PhotoAlbum> albumsList;

    public Photos() {
        jsonParser = new JSONParser();
    }

    public void parseUploadServer(String response, String method) {
        try {
            if (method.equals("Photos.getOwnerPhotoUploadServer")) {
                JSONObject json = jsonParser.parseJSON(response);
                ownerPhotoUploadServer = json.getJSONObject("response").getString("upload_url");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parse(String response) {
        try {
            list = new ArrayList<>();
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray photos = json.getJSONObject("response").getJSONArray("photos");
            for(int i = 0; i < photos.length(); i++) {
                JSONObject item = photos.getJSONObject(i);
                Photo photo = new Photo();
                photo.id = item.getLong("id");
                if(item.isNull("album_id")) {
                    photo.album_id = 0;
                } else {
                    photo.album_id = item.getLong("album_id");
                }
                photo.owner_id = item.getLong("owner_id");
                list.add(photo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parse(String response, PhotoAlbum album) {
        try {
            album.photos = new ArrayList<>();
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray photos = json.getJSONObject("response").getJSONArray("photos");
            for(int i = 0; i < photos.length(); i++) {
                JSONObject item = photos.getJSONObject(i);
                Photo photo = new Photo();
                photo.id = item.getLong("id");
                if(item.isNull("album_id")) {
                    photo.album_id = 0;
                } else {
                    photo.album_id = item.getLong("album_id");
                }
                photo.owner_id = item.getLong("owner_id");
                album.photos.add(photo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parseOnePhoto(String response) {
        try {
            if(list == null) {
                list = new ArrayList<>();
            }
            JSONObject json = jsonParser.parseJSON(response);
            JSONObject item = json.getJSONArray("response").getJSONObject(0);
            Photo photo = new Photo();
            photo.id = item.getLong("id");
            if(item.isNull("album_id")) {
                photo.album_id = 0;
            } else {
                photo.album_id = item.getLong("album_id");
            }
            photo.owner_id = item.getLong("owner_id");
            list.add(photo);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void parseAlbums(String response, DownloadManager dl_man, boolean clear) {
        try {
            if(albumsList == null || clear) {
                albumsList = new ArrayList<>();
            }
            ArrayList<PhotoAttachment> thumbnails = new ArrayList<>();
            JSONObject json = jsonParser.parseJSON(response);
            JSONArray albums = json.getJSONObject("response").getJSONArray("items");
            for(int i = 0; i < albums.length(); i++) {
                JSONObject item = albums.getJSONObject(i);
                PhotoAlbum album = new PhotoAlbum(item.getString("id"));
                album.title = item.getString("title");
                album.size = item.getLong("size");
                album.thumbnail_url = item.getString("thumb_src");
                albumsList.add(album);
                PhotoAttachment attachment = new PhotoAttachment();
                attachment.url = album.thumbnail_url;
                attachment.filename = String.format("photo_album_%s_%s",
                        album.ids[0], album.ids[1]);
                thumbnails.add(attachment);
            }
            dl_man.downloadPhotosToCache(thumbnails, "photo_albums");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getOwnerUploadServer(OvkAPIWrapper wrapper, long owner_id) {
        wrapper.sendAPIMethod("Photos.getOwnerPhotoUploadServer", String.format("owner_id=%s", owner_id));
    }

    public void getWallUploadServer(OvkAPIWrapper wrapper, long group_id) {
        wrapper.sendAPIMethod("Photos.getWallUploadServer", String.format("group_id=%s", group_id));
    }

    public void saveWallPhoto(OvkAPIWrapper wrapper, String photo, String hash) {
        wrapper.sendAPIMethod("Photos.saveWallPhoto", String.format("photo=%s&hash=%s", photo, hash));
    }


    public void getAlbums(OvkAPIWrapper wrapper, long owner_id, int count,
                          boolean need_system, boolean need_covers, boolean photo_sizes) {
        String bl_need_system;
        if(need_system) {
            bl_need_system = "1";
        } else {
            bl_need_system = "0";
        }
        String bl_need_covers;
        if(need_covers) {
            bl_need_covers = "1";
        } else {
            bl_need_covers = "0";
        }
        String bl_photo_sizes;
        if(photo_sizes) {
            bl_photo_sizes = "1";
        } else {
            bl_photo_sizes = "0";
        }

        wrapper.sendAPIMethod("Photos.getAlbums",
                String.format("owner_id=%s" +
                        "&count=%s" +
                        "&need_system=%s" +
                        "&need_covers=%s" +
                        "&photo_sizes=%s", owner_id, count, need_system, need_covers, photo_sizes));
    }

    public void getByAlbumId(OvkAPIWrapper wrapper, long owner_id, long album_id, int count) {
        wrapper.sendAPIMethod("Photos.get", "owner_id=%s&album_id=%s");
    }
}
