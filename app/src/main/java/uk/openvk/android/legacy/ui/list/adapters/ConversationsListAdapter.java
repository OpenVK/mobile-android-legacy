/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.Account;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.legacy.core.activities.ConversationActivity;

public class ConversationsListAdapter extends RecyclerView.Adapter<ConversationsListAdapter.Holder> {
    private final ImageLoader imageLoader;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Conversation> objects;
    public Account account;

    public ConversationsListAdapter(Context context, ArrayList<Conversation> items, Account account) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.account = account;

        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().clearDiskCache();
            ImageLoader.getInstance().clearMemoryCache();
        }
        this.imageLoader = ImageLoader.getInstance();
    }

    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public ConversationsListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ConversationsListAdapter.Holder(
                LayoutInflater.from(ctx).inflate(R.layout.list_item_conversation, parent, false)
        );
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

        private final TextView titleTv;
        private final TextView timeTv;
        private final ImageView avatarIv;
        private final RelativeLayout lastMsgLayout;
        private final TextView textTv;
        private final ImageView lastMsgAvatar;

        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            titleTv = view.findViewById(R.id.conversation_title);
            lastMsgLayout = view.findViewById(R.id.last_msg_rl);
            lastMsgAvatar = view.findViewById(R.id.last_msg_author_avatar);
            textTv = view.findViewById(R.id.conversation_text);
            timeTv = view.findViewById(R.id.conversation_time);
            avatarIv = view.findViewById(R.id.conversation_avatar);
        }

        @SuppressLint("SimpleDateFormat")
        void bind(final int position) {
            final Conversation item = getConversationItem(position);
            String lastMsgTimestamp;
            long lastMsgTimeMsec = TimeUnit.SECONDS.toMillis(item.lastMsgTime);

            if((System.currentTimeMillis() - lastMsgTimeMsec) < 86400000)
                lastMsgTimestamp = new SimpleDateFormat(" HH:mm ").format(lastMsgTimeMsec);
            else
                lastMsgTimestamp = (System.currentTimeMillis() - lastMsgTimeMsec) < 31536000000L ?
                    new SimpleDateFormat(" dd MMM ").format(lastMsgTimeMsec) :
                    new SimpleDateFormat(" dd.MM.yyyy ").format(lastMsgTimeMsec);

            timeTv.setText(lastMsgTimestamp);
            titleTv.setText(item.title);

            loadAvatars(position);

            if(item.peer_id != item.lastMsgAuthorId)
                lastMsgAvatar.setVisibility(View.VISIBLE);
            else
                lastMsgAvatar.setVisibility(View.GONE);

            if(item.lastMsgTime != 0 && item.lastMsgText != null) {
                if (item.lastMsgText.length() > 0) {
                    lastMsgLayout.setVisibility(View.VISIBLE);
                    textTv.setText(item.lastMsgText);
                } else {
                    lastMsgLayout.setVisibility(View.GONE);
                }
            } else {
                lastMsgLayout.setVisibility(View.GONE);
                timeTv.setVisibility(View.GONE);
            }

            try {
                if (item.lastMsgAuthorId == item.peer_id  && item.avatar != null) {
                    lastMsgAvatar.setVisibility(View.VISIBLE);
                } else if (item.lastMsgAuthorId == account.id && account.user.avatar != null) {
                    lastMsgAvatar.setVisibility(View.VISIBLE);
                }
            } catch (Exception ex) {
                lastMsgAvatar.setVisibility(View.GONE);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx instanceof AppActivity)
                        getConversation(item);
                }
            });
        }

        private void loadAvatars(int position) {
            String instance = ((OvkApplication) ctx.getApplicationContext()).getCurrentInstance();

            Conversation conv = (Conversation) getItem(position);

            Bitmap convBitmap = imageLoader.loadImageSync(
                    String.format("file://%s/%s/photos_cache/conversations_avatars/avatar_%s",
                            ctx.getCacheDir(), instance, conv.peer_id)
            );

            Bitmap authorBitmap = imageLoader.loadImageSync(
                    String.format("file://%s/%s/photos_cache/profile_avatars/avatar_%s",
                            ctx.getCacheDir(), instance, conv.lastMsgAuthorId)
            );

            if (convBitmap != null) {
                conv.avatar = convBitmap;
                avatarIv.setImageBitmap(conv.avatar);
            } else {
                int placeholderId;

                switch (conv.peer_type) {
                    case "group":
                    case "chat":
                        placeholderId = R.drawable.ic_chat_multi;
                        break;
                    default:
                        placeholderId = R.drawable.user_placeholder_chat;
                        break;
                }

                avatarIv.setImageDrawable(
                        ctx.getResources().getDrawable(placeholderId)
                );
            }

            if(authorBitmap != null) {
                conv.lastMsgAvatar = authorBitmap;
                lastMsgAvatar.setImageBitmap(authorBitmap);
            } else {
                lastMsgAvatar.setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.photo_loading)
                );
            }
        }
    }

    private void getConversation(Conversation item) {
        Intent intent = new Intent(ctx, ConversationActivity.class);
        try {
            intent.putExtra("peer_id", item.peer_id);
            if(item.peer_type != null)
                intent.putExtra("peer_type", item.peer_type);
            intent.putExtra("members_count", item.members_count);
            intent.putExtra("conv_title", item.title);
            intent.putExtra("online", item.online);
            ctx.startActivity(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}
