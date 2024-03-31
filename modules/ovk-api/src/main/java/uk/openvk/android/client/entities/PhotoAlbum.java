/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK API Client Library for Android.
 *
 *  OpenVK API Client Library for Android is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along
 *  with this program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.client.entities;

import java.util.ArrayList;

public class PhotoAlbum {
    public long[] ids = new long[2];
    public String title;
    public long size;
    public String thumbnail_url;
    public ArrayList<Photo> photos;
    public PhotoAlbum(String str_ids) {
        String[] ids = str_ids.split("_");
        try {
            if (ids.length >= 2) {
                long owner_id = Long.parseLong(ids[0]);
                long album_id = Long.parseLong(ids[1]);
                this.ids[0] = owner_id;
                this.ids[1] = album_id;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PhotoAlbum(long owner_id, long album_id) {
        ids = new long[2];
        this.ids[0] = owner_id;
        this.ids[1] = album_id;
    }
}
