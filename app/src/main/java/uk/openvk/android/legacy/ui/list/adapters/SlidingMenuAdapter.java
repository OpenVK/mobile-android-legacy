/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuObject;

public class SlidingMenuAdapter extends RecyclerView.Adapter<SlidingMenuAdapter.Holder> {
    Context ctx;
    ArrayList<SlidingMenuObject> objects;
    boolean extendedMenu;

    public SlidingMenuAdapter(Context context, ArrayList<SlidingMenuObject> items, boolean extendedMenu) {
        ctx = context;
        objects = items;
        this.extendedMenu = extendedMenu;
    }

    @Override
    public SlidingMenuAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutRes;

        if(extendedMenu)
            layoutRes = R.layout.list_item_sliding_menu_3;
        else {
            switch (viewType) {
                case SlidingMenuObject.TYPE_CATEGORY:
                    layoutRes = R.layout.list_item_sliding_menu_category;
                    break;
                case SlidingMenuObject.TYPE_PUBLIC_PAGE:
                    layoutRes = R.layout.list_item_sliding_menu_2;
                    break;
                default:
                    layoutRes = R.layout.list_item_sliding_menu;
                    break;
            }
        }

        return new SlidingMenuAdapter.Holder(
                LayoutInflater.from(ctx).inflate(
                        layoutRes, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
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

    SlidingMenuObject getMenuItem(int position) {
        return objects.get(position);
    }

    public void updateArray(ArrayList<SlidingMenuObject> array) {
        objects = array;
    }

    @Override
    public int getItemViewType(int position) {
        return getMenuItem(position).type;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View view;
        private final TextView itemTextView;
        private final TextView itemCounterTextView;
        private final ImageView itemIcon;

        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            itemTextView = view.findViewById(R.id.leftmenu_text);
            itemCounterTextView = view.findViewById(R.id.leftmenu_counter);
            itemIcon = view.findViewById(R.id.leftmenu_icon);
        }

        void bind(final int position) {
            SlidingMenuObject item = getMenuItem(position);
            itemTextView.setText(item.name);

            if(itemCounterTextView != null) {
                if (item.counter == 0) {
                    itemCounterTextView.setVisibility(View.GONE);
                } else {
                    itemCounterTextView.setVisibility(View.VISIBLE);
                    itemCounterTextView.setText(String.valueOf(item.counter));
                }
            }

            if(itemIcon != null) {
                if (item.icon != null) {
                    itemIcon.setImageDrawable(item.icon);
                    itemIcon.setVisibility(View.VISIBLE);
                } else
                    itemIcon.setVisibility(View.GONE);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx instanceof AppActivity) {
                        if(extendedMenu)
                            ((AppActivity) ctx).onAccountSlidingMenuItemClicked(position);
                        else
                            ((AppActivity) ctx).onSlidingMenuItemClicked(position, true);
                    }
                }
            });

            ImageView onlineView = view.findViewById(R.id.leftmenu_online);

            if(onlineView != null)
                onlineView.setVisibility(View.GONE);
        }
    }
}