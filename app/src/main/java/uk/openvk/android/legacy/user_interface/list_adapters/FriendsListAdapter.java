package uk.openvk.android.legacy.user_interface.list_adapters;

import android.content.Context;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.user_interface.text.CenteredImageSpan;

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
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
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

        final Friend item = getFriend(position);
        if(item.verified) {
            String name = String.format("%s %s  ", item.first_name, item.last_name);
            SpannableStringBuilder sb = new SpannableStringBuilder(name);
            ImageSpan imageSpan;
            imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black);
            ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 * ctx.getResources().getDisplayMetrics().density));
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
        } else {
            ((ImageView) view.findViewById(R.id.flist_item_photo)).setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
        }

        if(item.from_mobile) {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_online_mobile));
        } else {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_online));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).hideSelectedItemBackground(position);
                    ((AppActivity) ctx).showProfile(item.id);
                } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
                    ((FriendsIntentActivity) ctx).hideSelectedItemBackground(position);
                    ((FriendsIntentActivity) ctx).showProfile(item.id);
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

