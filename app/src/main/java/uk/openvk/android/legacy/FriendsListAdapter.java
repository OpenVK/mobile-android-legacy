package uk.openvk.android.legacy;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<FriendsListItem> objects;
    public boolean opened_sliding_menu;

    FriendsListAdapter(Context context, ArrayList<FriendsListItem> items) {
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

    FriendsListItem getFriendsListItem(int position) {
        return ((FriendsListItem) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.friend_list_item, parent, false);
        }

        FriendsListItem item = getFriendsListItem(position);
        ((TextView) view.findViewById(R.id.flist_item_text)).setText(item.name);
        if(item.online == 1) {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AppActivity) ctx).hideSelectedItemBackground(position);
            }
        });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */

        view.setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
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

