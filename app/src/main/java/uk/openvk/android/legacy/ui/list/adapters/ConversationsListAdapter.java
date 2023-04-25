package uk.openvk.android.legacy.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.api.models.Conversation;

public class ConversationsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Conversation> objects;
    public Account account;
    public boolean opened_sliding_menu;

    public ConversationsListAdapter(Context context, ArrayList<Conversation> items, Account account) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.account = account;
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

    @SuppressLint("SimpleDateFormat")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_conversation, parent, false);
        }

        Conversation item = getConversationItem(position);
        ((TextView) view.findViewById(R.id.conversation_title)).setText(item.title);
        String lastMsgTimestamp;
        if((System.currentTimeMillis() - (TimeUnit.SECONDS.toMillis(item.lastMsgTime))) < 86400000) {
            lastMsgTimestamp = new SimpleDateFormat(" HH:mm ").format(TimeUnit.SECONDS.toMillis(item.lastMsgTime));
        } else if((System.currentTimeMillis() - (TimeUnit.SECONDS.toMillis(item.lastMsgTime))) < 31536000000L) {
            lastMsgTimestamp = new SimpleDateFormat(" dd MMM ").format(TimeUnit.SECONDS.toMillis(item.lastMsgTime));
        } else {
            lastMsgTimestamp = new SimpleDateFormat(" dd.MM.yyyy ").format(TimeUnit.SECONDS.toMillis(item.lastMsgTime));
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

        if(item.avatar_url.length() > 0 && item.avatar != null) {
            ((ImageView) view.findViewById(R.id.conversation_avatar)).setImageBitmap(item.avatar);
        }

        if(item.lastMsgAuthorId == item.peer_id) {
            ((ImageView) view.findViewById(R.id.last_msg_author_avatar)).setImageBitmap(account.user.avatar);
            ((ImageView) view.findViewById(R.id.last_msg_author_avatar)).setVisibility(View.GONE);
        } else if(item.lastMsgAuthorId == account.id) {
            ((ImageView) view.findViewById(R.id.last_msg_author_avatar)).setImageBitmap(account.user.avatar);
            ((ImageView) view.findViewById(R.id.last_msg_author_avatar)).setVisibility(View.VISIBLE);
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
