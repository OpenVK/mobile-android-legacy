package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.ConversationActivity;
import uk.openvk.android.legacy.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.list_items.FriendsListItem;
import uk.openvk.android.legacy.list_items.MessagesListItem;
import uk.openvk.android.legacy.listeners.SwipeListener;

public class MessagesListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<MessagesListItem> objects;
    public boolean opened_sliding_menu;

    public MessagesListAdapter(Context context, ArrayList<MessagesListItem> items) {
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

    MessagesListItem getMessagesListItem(int position) {
        return ((MessagesListItem) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        MessagesListItem item = getMessagesListItem(position);
        View view = convertView;
        if (view == null) {
            if(item.isIncoming) {
                Log.d("OpenVK Legacy", "Boolean: " + item.isIncoming);
                view = inflater.inflate(R.layout.message_in, parent, false);
            } else {
                view = inflater.inflate(R.layout.message_out, parent, false);
            }
        }

        if(((ImageView) view.findViewById(R.id.msg_failed)) != null) {
            ((ImageView) view.findViewById(R.id.msg_failed)).setVisibility(View.GONE);
        }

        if((ProgressBar) view.findViewById(R.id.msg_progress) != null) {
            ((ProgressBar) view.findViewById(R.id.msg_progress)).setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.msg_text)).setText(item.text);

        view.setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return super.onTouch(v, event);
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
                    ((ConversationActivity) ctx).hideSelectedItemBackground(position);
                }
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
