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
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.ConversationActivity;
import uk.openvk.android.legacy.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.layouts.IncomingMessageLayout;
import uk.openvk.android.legacy.layouts.MessagesDateSeparator;
import uk.openvk.android.legacy.layouts.OutcomingMessageLayout;
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
            view = inflater.inflate(R.layout.message_item, parent, false);
            view.findViewById(R.id.incoming_msg).setVisibility(View.GONE);
            Date startOfDay = new Date(TimeUnit.SECONDS.toMillis(item.timestamp_int));
            startOfDay.setHours(0);
            startOfDay.setMinutes(0);
            startOfDay.setSeconds(0);
            Date prev_startOfDay = null;
            if(position > 0) {
                prev_startOfDay = new Date(TimeUnit.SECONDS.toMillis(getMessagesListItem(position - 1).timestamp_int));
                prev_startOfDay.setHours(0);
                prev_startOfDay.setMinutes(0);
                prev_startOfDay.setSeconds(0);
            }
            if(prev_startOfDay != null)
            Log.d("StartOfDay", "Comparision result: " + startOfDay.compareTo(prev_startOfDay));
            ((MessagesDateSeparator) view.findViewById(R.id.date_separator)).setVisibility(View.GONE);
            if(item.isIncoming) {
                ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).setVisibility(View.VISIBLE);
                ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).setVisibility(View.GONE);
            } else {
                ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).setVisibility(View.GONE);
                ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).setVisibility(View.VISIBLE);
            }
        }

        if(!item.isError) {
            ((ImageView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_failed)).setVisibility(View.GONE);
        }

        ((ProgressBar) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_progress)).setVisibility(View.GONE);

        if(item.text.length() > 12) {
            ((TextView) ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.GONE);
            ((TextView) ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.VISIBLE);
            ((TextView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.GONE);
            ((TextView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.VISIBLE);
            ((TextView) ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.GONE);
            ((TextView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.VISIBLE);
            ((TextView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.GONE);
        }

        ((TextView) ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_text)).setText(item.text);
        ((TextView) ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_right)).setText(item.timestamp);
        ((TextView) ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_bottom)).setText(item.timestamp);
        ((TextView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_text)).setText(item.text);
        ((TextView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_right)).setText(item.timestamp);
        ((TextView) ((OutcomingMessageLayout) view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_bottom)).setText(item.timestamp);

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

    public void setArray(ArrayList<MessagesListItem> array) {
        objects = array;
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }
}
