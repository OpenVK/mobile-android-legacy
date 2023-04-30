package uk.openvk.android.legacy.ui.list.items;

import android.graphics.Bitmap;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class SearchResultItem {
    public int id;
    public String title;
    public String subtitle;
    public Bitmap avatar;
    public int online;

    public SearchResultItem(int ovk_id, String item_title, String item_subtitle, Bitmap author_avatar,
                            int user_online) {
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
