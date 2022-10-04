package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.api.models.Conversation;

public class ConversationsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Conversation> objects;
    public boolean opened_sliding_menu;

    public ConversationsListAdapter(Context context, ArrayList<Conversation> items) {
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

    Conversation getConversationItem(int position) {
        return ((Conversation) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.conversation_item, parent, false);
        }

        Conversation item = getConversationItem(position);
        ((TextView) view.findViewById(R.id.conversation_title)).setText(item.title);
        String lastMsgTimestamp;
        if((System.currentTimeMillis() - (item.lastMsgTime * 1000)) < 86400000) {
            lastMsgTimestamp = new SimpleDateFormat(" HH:mm ").format(item.lastMsgTime);
        } else {
            lastMsgTimestamp = new SimpleDateFormat(" dd.MM HH:mm ").format(item.lastMsgTime);
        }
        ((TextView) view.findViewById(R.id.conversation_time)).setText(lastMsgTimestamp);
        if(item.lastMsgTime != 0 && item.lastMsgText != null) {
            if (item.lastMsgText.length() > 0) {
                ((RelativeLayout) view.findViewById(R.id.last_msg_rl)).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.conversation_text)).setText(item.lastMsgText);
            } else {
                ((RelativeLayout) view.findViewById(R.id.last_msg_rl)).setVisibility(View.GONE);
            }
        } else {
            ((RelativeLayout) view.findViewById(R.id.last_msg_rl)).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.conversation_time)).setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                ((AppActivity) ctx).getConversation(position);
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
