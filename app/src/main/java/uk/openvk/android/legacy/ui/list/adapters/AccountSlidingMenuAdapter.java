package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuItem;

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

public class AccountSlidingMenuAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<SlidingMenuItem> objects;
    public boolean opened_sliding_menu;
    public AccountSlidingMenuAdapter(Context context, ArrayList<SlidingMenuItem> items) {
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
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    SlidingMenuItem getSlidingMenuItem(int position) {
        return ((SlidingMenuItem) getItem(position));
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_sliding_menu_3, parent, false);
        }

        final int position = i;

        SlidingMenuItem item = getSlidingMenuItem(i);
        ((TextView) view.findViewById(R.id.leftmenu_text)).setText(item.name);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AppActivity) ctx).onAccountSlidingMenuItemClicked(position);
            }
        });
        return view;
    }
}
