package uk.openvk.android.legacy.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.api.entities.Conversation;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class ConversationsListAdapter extends RecyclerView.Adapter<ConversationsListAdapter.Holder> {
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

    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public ConversationsListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationsListAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_conversation, parent, false));
    }

    @Override
    public void onBindViewHolder(ConversationsListAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    Conversation getConversationItem(int position) {
        return ((Conversation) getItem(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        public View view;

        public Holder(View convertView) {
            super(convertView);
            view = convertView;
        }

        void bind(final int position) {
            Conversation item = getConversationItem(position);
            ((TextView) view.findViewById(R.id.conversation_title)).setText(item.title);
            String lastMsgTimestamp;
            if((System.currentTimeMillis() - (TimeUnit.SECONDS.toMillis(item.lastMsgTime))) < 86400000) {
                lastMsgTimestamp = new SimpleDateFormat(" HH:mm ").format(TimeUnit.SECONDS
                        .toMillis(item.lastMsgTime));
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
                ((ImageView) view.findViewById(R.id.last_msg_author_avatar))
                        .setImageBitmap(account.user.avatar);
                ((ImageView) view.findViewById(R.id.last_msg_author_avatar)).setVisibility(View.GONE);
            } else if(item.lastMsgAuthorId == account.id) {
                ((ImageView) view.findViewById(R.id.last_msg_author_avatar))
                        .setImageBitmap(account.user.avatar);
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
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}
