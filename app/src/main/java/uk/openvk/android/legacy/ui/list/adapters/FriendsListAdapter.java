package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.core.activities.intents.FriendsIntentActivity;
import uk.openvk.android.legacy.core.fragments.FriendsFragment;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

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

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.Holder> {
    private final FriendsFragment friendsFragment;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Friend> objects;
    public boolean opened_sliding_menu;

    public FriendsListAdapter(Context context, FriendsFragment friendsFragment, ArrayList<Friend> items) {
        this.friendsFragment = friendsFragment;
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public Friend getItem(int position) {
        return objects.get(position);
    }

    @Override
    public FriendsListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FriendsListAdapter.Holder(
                LayoutInflater.from(ctx).inflate(R.layout.list_item_friend, parent, false));
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
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
    }

    Friend getFriend(int position) {
        return ((Friend) getItem(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        private final View view;
        public TextView item_id;
        public TextView item_name;
        public ImageView item_avatar;
        public ImageView item_online;
        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            item_name = (view.findViewById(R.id.flist_item_text));
            item_avatar = (view.findViewById(R.id.flist_item_photo));
            item_online = (view.findViewById(R.id.flist_item_online));
        }

        void bind(final int position) {
            final Friend item = getFriend(position);
            if(item.verified) {
                String name = String.format("%s %s  ", item.first_name, item.last_name);
                SpannableStringBuilder sb = new SpannableStringBuilder(name);
                ImageSpan imageSpan;
                imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black);
                ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 *
                        ctx.getResources().getDisplayMetrics().density));
                sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                item_name.setText(sb);
            } else {
                item_name.setText(
                        String.format("%s %s", item.first_name, item.last_name));
            }
            if(item.online) {
                ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.VISIBLE);
            } else {
                ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.GONE);
            }
            if(item.avatar != null) {
                item_avatar.setImageBitmap(item.avatar);
            } else {
                item_avatar.setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.photo_loading));
            }

            if(item.from_mobile) {
                item_online.setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.ic_online_mobile));
            } else {
                item_online.setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.ic_online));
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        friendsFragment.hideSelectedItemBackground(position);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    showProfile(item.id);
                }
            });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */
        }

        private void showProfile(Long user_id) {
            if(ctx instanceof AppActivity) {
                AppActivity app_a = ((AppActivity) ctx);
                if (user_id != app_a.ovk_api.account.id) {
                    String url = "openvk://ovk/id" + user_id;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    i.setPackage("uk.openvk.android.legacy");
                    ctx.startActivity(i);
                } else {
                    app_a.openAccountProfile();
                }
            } else {
                String url = "openvk://ovk/id" + user_id;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                i.setPackage("uk.openvk.android.legacy");
                ctx.startActivity(i);
            }
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

}

