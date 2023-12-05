package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.QuickSearchActivity;
import uk.openvk.android.legacy.api.entities.Group;

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

public class GroupsSearchResultAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Group> objects;
    public boolean loadAvatars;

    public GroupsSearchResultAdapter(Context context, ArrayList<Group> items) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Group getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    Group getGroup(int position) {
        return ((Group) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_search_result, parent, false);
        }

        Group item = getGroup(position);
        ((TextView) view.findViewById(R.id.sr_list_item_text)).setText(item.name);
        ((TextView) view.findViewById(R.id.sr_list_item_subtext)).setVisibility(View.GONE);
        ((ImageView) view.findViewById(R.id.sr_list_item_online)).setVisibility(View.GONE);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
                    ((QuickSearchActivity) ctx).hideSelectedItemBackground(position);
                    ((QuickSearchActivity) ctx).showGroup(position);
                }
            }
        });
        if(loadAvatars) {
            loadAvatar(view, item.id);
        }

        return view;
    }

    private void loadAvatar(View view, long id) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap avatar = BitmapFactory.decodeFile(
                    ctx.getCacheDir() + "/photos_cache/group_avatars/avatar_"
                            + id, options);
            if(avatar != null) {
                ((ImageView) view.findViewById(R.id.sr_list_item_photo)).setImageBitmap(avatar);
            } else {
                ((ImageView) view.findViewById(R.id.sr_list_item_photo))
                        .setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
            }
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        }
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }

}

