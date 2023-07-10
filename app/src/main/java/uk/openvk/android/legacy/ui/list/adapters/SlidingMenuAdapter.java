package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuItem;

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

public class SlidingMenuAdapter extends RecyclerView.Adapter<SlidingMenuAdapter.Holder> {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<SlidingMenuItem> objects;
    public boolean opened_sliding_menu;

    public SlidingMenuAdapter(Context context, ArrayList<SlidingMenuItem> items) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public SlidingMenuAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(inflater.inflate(R.layout.list_item_sliding_menu, parent, false));
    }

    @Override
    public void onBindViewHolder(SlidingMenuAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    SlidingMenuItem getSlidingMenuItem(int position) {
        return objects.get(position);
    }

    public class Holder extends RecyclerView.ViewHolder {

        private View view;

        public Holder(View convertView) {
            super(convertView);
            this.view = convertView;
        }

        void bind(final int position) {
            view.setTag("account_menu_list" + position);

            String tag = (String) view.getTag();

            SlidingMenuItem item = getSlidingMenuItem(position);
            ((TextView) view.findViewById(R.id.leftmenu_text)).setText(item.name);
            if (item.counter == 0) {
                ((TextView) view.findViewById(R.id.leftmenu_counter)).setVisibility(View.GONE);
            } else {
                ((TextView) view.findViewById(R.id.leftmenu_counter)).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.leftmenu_counter)).setText("" + item.counter);
            }
            ((ImageView) view.findViewById(R.id.leftmenu_icon)).setImageDrawable(item.icon);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((AppActivity) ctx).onSlidingMenuItemClicked(position, true);
                }
            });
        }
    }
}
