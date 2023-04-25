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
import uk.openvk.android.legacy.api.models.Group;

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

public class GroupsSearchResultAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Group> objects;

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

        return view;
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }

}

