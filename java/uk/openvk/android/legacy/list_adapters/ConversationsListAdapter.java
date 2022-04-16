package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.list_items.ConversationsListItem;
import uk.openvk.android.legacy.listeners.SwipeListener;

public class ConversationsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<ConversationsListItem> objects;
    public boolean opened_sliding_menu;

    public ConversationsListAdapter(Context context, ArrayList<ConversationsListItem> items) {
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

    ConversationsListItem getConversationItem(int position) {
        return ((ConversationsListItem) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.conversation_item, parent, false);
        }

        ConversationsListItem item = getConversationItem(position);
        ((TextView) view.findViewById(R.id.conversation_title)).setText(item.title);
        ((TextView) view.findViewById(R.id.conversation_time)).setText(item.lastMsgTimestamp);
        if(item.lastMsgText.length() > 0) {
            ((RelativeLayout) view.findViewById(R.id.last_msg_rl)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.conversation_text)).setText(item.lastMsgText);
        } else {
            ((RelativeLayout) view.findViewById(R.id.last_msg_rl)).setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).getConversation(position);
                }
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
