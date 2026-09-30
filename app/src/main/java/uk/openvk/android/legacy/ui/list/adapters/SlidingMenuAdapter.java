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
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuObject;

public class SlidingMenuAdapter extends RecyclerView.Adapter<SlidingMenuAdapter.Holder> {
    Context ctx;
    ArrayList<SlidingMenuObject> objects;
    boolean extendedMenu;
    private ImageLoader imageLoader;

    public SlidingMenuAdapter(Context context, ArrayList<SlidingMenuObject> items, boolean extendedMenu) {
        ctx = context;
        objects = items;
        this.extendedMenu = extendedMenu;

        imageLoader = ImageLoader.getInstance();
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
        private ImageView itemAvatar;

        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            itemTextView = view.findViewById(R.id.leftmenu_text);
            itemCounterTextView = view.findViewById(R.id.leftmenu_counter);
            itemIcon = view.findViewById(R.id.leftmenu_icon);
            itemAvatar = view.findViewById(R.id.leftmenu_photo);
        }

        void bind(final int position) {
            final SlidingMenuObject item = getMenuItem(position);
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
                        else if(item.type == SlidingMenuObject.TYPE_MENU_ITEM)
                            ((AppActivity) ctx).onSlidingMenuItemClicked(position, true);
                        else if(item.embed != null) {
                            String url;
                            if (item.embed instanceof Group) {
                                url = "openvk://ovk/club" + item.embed.id;
                            } else {
                                url = "openvk://ovk/id" + item.embed.id;
                            }
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setPackage("uk.openvk.android.legacy");
                            i.setData(Uri.parse(url));
                            ctx.startActivity(i);
                        }
                    }
                }
            });

            loadAvatar(position);

            ImageView onlineView = view.findViewById(R.id.leftmenu_online);

            if(onlineView != null)
                onlineView.setVisibility(View.GONE);
        }

        private void loadAvatar(int position) {
            String instance = ((OvkApplication) ctx.getApplicationContext()).getCurrentInstance();

            Friend friend;
            Group group;

            if(getMenuItem(position).embed instanceof Friend) {
                friend = (Friend) getMenuItem(position).embed;

                Bitmap bitmap = imageLoader.loadImageSync(
                        String.format("file://%s/%s/photos_cache/friend_avatars/avatar_%s",
                                ctx.getCacheDir(), instance, friend.id)
                );


                if (bitmap != null) {
                    friend.avatar = bitmap;
                    itemAvatar.setImageBitmap(friend.avatar);
                } else {
                    itemAvatar.setImageDrawable(
                            ctx.getResources().getDrawable(R.drawable.photo_loading));
                }
            } else if(getMenuItem(position).embed instanceof Group) {
                group = (Group) getMenuItem(position).embed;

                Bitmap bitmap = imageLoader.loadImageSync(
                        String.format("file://%s/%s/photos_cache/group_avatars/avatar_%s",
                                ctx.getCacheDir(), instance, group.id)
                );


                if (bitmap != null) {
                    group.avatar = bitmap;
                    itemAvatar.setImageBitmap(group.avatar);
                } else {
                    itemAvatar.setImageDrawable(
                            ctx.getResources().getDrawable(R.drawable.photo_loading));
                }
            }
        }
    }
}