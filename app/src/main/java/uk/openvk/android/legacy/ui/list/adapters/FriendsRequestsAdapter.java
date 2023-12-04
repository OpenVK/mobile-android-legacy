package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.fragments.FriendsFragment;

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

public class FriendsRequestsAdapter extends RecyclerView.Adapter<FriendsRequestsAdapter.Holder> {

    private ArrayList<Friend> items = new ArrayList<>();
    private Context ctx;
    private FriendsFragment parent;
    public LruCache memCache;

    public FriendsRequestsAdapter(Context context, FriendsFragment parent, ArrayList<Friend> friends) {
        ctx = context;
        this.parent = parent;
        items = friends;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_friends_req, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
    }

    public Friend getItem(int position) {
       return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final TextView req_name;
        public final TextView req_info;
        private final View convertView;
        private final ImageView avatar;
        private final FrameLayout reg_btn;
        private final RelativeLayout req_wrap;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.req_name = (TextView) view.findViewById(R.id.friend_req_name);
            this.req_info = (TextView) view.findViewById(R.id.friend_req_info);
            this.avatar = (ImageView) view.findViewById(R.id.friend_req_photo);
            this.reg_btn = (FrameLayout) view.findViewById(R.id.friend_req_btn_add);
            this.req_wrap = (RelativeLayout) view.findViewById(R.id.friend_req_wrap);
        }

        void bind(final int position) {
            final Friend item = getItem(position);
            req_name.setText(String.format("%s %s", item.first_name, item.last_name));
            req_info.setText("");
            if(item.avatar != null) {
                this.avatar.setImageBitmap(item.avatar);
            } else {
                this.avatar.setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
            }

            req_wrap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        showProfile(item.id);
                    }
                }
            });

            reg_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx instanceof AppActivity) {
                        ((FriendsFragment) parent).requests_cursor_index = position;
                        Global.addToFriends(((AppActivity) ctx).ovk_api, item.id);
                    }
                }
            });

        }

        private void showProfile(int user_id) {
            if(ctx instanceof AppActivity) {
                AppActivity app_a = ((AppActivity) ctx);
                if (user_id != app_a.ovk_api.account.id) {
                    String url = "openvk://profile/" + "id" + user_id;
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    i.setPackage("uk.openvk.android.legacy");
                    ctx.startActivity(i);
                } else {
                    app_a.openAccountProfile();
                }
            } else {
                String url = "openvk://profile/" + "id" + user_id;
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

    public void setArray(ArrayList<Friend> array) {
        items = array;
    }
}