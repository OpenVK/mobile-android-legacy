package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.api.models.Friend;

public class FriendsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Friend> objects;
    public boolean opened_sliding_menu;

    public FriendsListAdapter(Context context, ArrayList<Friend> items) {
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

    Friend getFriend(int position) {
        return ((Friend) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.friend_list_item, parent, false);
        }

        Friend item = getFriend(position);
        if(item.verified) {
            String name = String.format("%s %s  ", item.first_name, item.last_name);
            SpannableStringBuilder sb = new SpannableStringBuilder(name);
            ImageSpan imageSpan = new ImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black, DynamicDrawableSpan.ALIGN_BASELINE);
            sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) view.findViewById(R.id.flist_item_text)).setText(sb);
        } else {
            ((TextView) view.findViewById(R.id.flist_item_text)).setText(String.format("%s %s", item.first_name, item.last_name));
        }
        if(item.online) {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.GONE);
        }
        if(item.avatar != null) {
            ((ImageView) view.findViewById(R.id.flist_item_photo)).setImageBitmap(item.avatar);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).hideSelectedItemBackground(position);
                    ((AppActivity) ctx).showProfile(position);
                } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
                    ((FriendsIntentActivity) ctx).hideSelectedItemBackground(position);
                    ((FriendsIntentActivity) ctx).showProfile(position);
                }
            }
        });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */

        return view;
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }

}

