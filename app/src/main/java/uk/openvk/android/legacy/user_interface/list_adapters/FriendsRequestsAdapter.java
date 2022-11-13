package uk.openvk.android.legacy.user_interface.list_adapters;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Friends;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.layouts.FriendsLayout;

public class FriendsRequestsAdapter extends RecyclerView.Adapter<FriendsRequestsAdapter.Holder> {

    private ArrayList<Friend> items = new ArrayList<>();
    private Context ctx;
    private View parent;
    public LruCache memCache;

    public FriendsRequestsAdapter(Context context, View parent, ArrayList<Friend> friends) {
        ctx = context;
        this.parent = parent;
        items = friends;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.friends_req_item, parent, false));
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
                        ((AppActivity) ctx).showProfile(item.id);
                    }
                }
            });

            reg_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((FriendsLayout) parent).requests_cursor_index = position;
                        ((AppActivity) ctx).addToFriends(item.id);
                    }
                }
            });

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