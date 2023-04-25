package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.QuickSearchActivity;
import uk.openvk.android.legacy.api.models.User;

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

public class UsersSearchResultAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<User> objects;

    public UsersSearchResultAdapter(Context context, ArrayList<User> items) {
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
    public User getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    User getUser(int position) {
        return ((User) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_search_result, parent, false);
        }

        User item = getUser(position);
        ((TextView) view.findViewById(R.id.sr_list_item_text)).setText(String.format("%s %s", item.first_name, item.last_name));
        if(item.city == null) {
            item.city = "";
        }
        if(item.city.length() > 0) {
            ((TextView) view.findViewById(R.id.sr_list_item_subtext)).setText(item.city);
        } else {
            ((TextView) view.findViewById(R.id.sr_list_item_subtext)).setVisibility(View.GONE);
        }
        if(item.online) {
            ((ImageView) view.findViewById(R.id.sr_list_item_online)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.sr_list_item_online)).setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
                    ((QuickSearchActivity) ctx).hideSelectedItemBackground(position);
                    ((QuickSearchActivity) ctx).showProfile(position);
                }
            }
        });

        return view;
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }

}

