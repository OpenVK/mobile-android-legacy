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

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.entities.ChatAction;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.client.entities.Message;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.ConversationActivity;
import uk.openvk.android.legacy.ui.views.MediaAttachmentsView;
import uk.openvk.android.legacy.ui.views.base.TightTextView;

public class MessagesHistoryAdapter extends RecyclerView.Adapter<MessagesHistoryAdapter.Holder> {
    private final Context ctx;
    private final Conversation conv;
    private ArrayList<Message> history;
    private ImageLoader imageLoader;
    private String instance;

    public MessagesHistoryAdapter(Context ctx, Conversation conv, ArrayList<Message> history) {
        this.conv = conv;
        this.ctx = ctx;
        this.history = new ArrayList<>();
        this.history.addAll(history);

        if(history != null) {
            int offset = 0;

            for (int i = 1; i < history.size(); i++) {
                if(isNewDay(history, i)) {
                    Message msg = new Message(LazyEntity.LAYOUT_HEADER);
                    msg.timestamp_long = history.get(i - 1).timestamp_long;
                    this.history.add(i + offset, msg);
                    offset++;
                }
            }

            Message msg = new Message(LazyEntity.LAYOUT_HEADER);
            msg.timestamp_long = getMessage(getItemCount() - 1).timestamp_long;
            this.history.add(getItemCount(), msg);

            if (ImageLoader.getInstance().isInited()) {
                ImageLoader.getInstance().clearDiskCache();
                ImageLoader.getInstance().clearMemoryCache();
            }
            this.imageLoader = ImageLoader.getInstance();
        }

        instance = ((OvkApplication) ctx.getApplicationContext()).getCurrentInstance();
    }

    public boolean isNewDay(ArrayList<Message> history, int position) {

        if(position == 0)
            return false;

        if(position >= history.size())
            return true;

        long timestamp = history.get(position).timestamp_long;
        long prevTimestamp = history.get(position - 1).timestamp_long;

        Date startOfDay = new Date(
                TimeUnit.SECONDS.toMillis(timestamp)
        );

        startOfDay.setHours(0);
        startOfDay.setMinutes(0);
        startOfDay.setSeconds(0);

        Date prevStartOfDay = new Date(
                TimeUnit.SECONDS.toMillis(prevTimestamp)
        );
        prevStartOfDay.setHours(0);
        prevStartOfDay.setMinutes(0);
        prevStartOfDay.setSeconds(0);

        return startOfDay.compareTo(prevStartOfDay) < 0;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutRes;

        switch (viewType) {
            case 0x82:  // if is incoming message
                layoutRes = R.layout.list_item_msg_incoming;
                break;
            case 0x81:  // if is outcoming message
                layoutRes = R.layout.list_item_msg_outcoming;
                break;
            default:
                layoutRes = R.layout.list_item_chat_action;
                break;
        }

        return new Holder(
                LayoutInflater.from(ctx).inflate(layoutRes, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    public Message getMessage(int position) {
        return history.get(position);
    }

    @Override
    public int getItemCount() {
        if(history != null)
            return history.size();
        else
            return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = getMessage(position);

        if(msg.action != null)
            return msg.action.type;
        else if(msg.getEntityType() == LazyEntity.REAL_ENTITY)
            return msg.isIncoming ? 0x82 : 0x81;
        else
            return 0x80;
    }

    public void addMessage(Message message) {
        history.add(0, message);
        notifyItemInserted(0);
    }

    public void deleteMessage(long msgId) {
        int position = getMessagePosition(msgId);
        if(position < 0)
            return;

        history.remove(position);
        notifyItemRemoved(position);
    }

    private int getMessagePosition(long msgId) {
        for (int i = 0; i < history.size(); i++) {
            if(msgId == history.get(i).id)
                return i;
        }
        return -1;
    }

    public class Holder extends RecyclerView.ViewHolder {

        private final ProgressBar progressBar;
        private final ImageView msgErrorIcon;
        private final TightTextView msgText;
        private final TextView msgTimeRightTv;
        private final TextView msgTimeBottomTv;
        private final ImageView msgAuthorAvatar;
        private final MediaAttachmentsView msgAttachView;

        public Holder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.msg_progress);
            msgErrorIcon = itemView.findViewById(R.id.msg_failed);
            msgText = itemView.findViewById(R.id.msg_text);
            msgTimeRightTv = itemView.findViewById(R.id.msg_time_right);
            msgTimeBottomTv = itemView.findViewById(R.id.msg_time_bottom);
            msgAuthorAvatar = itemView.findViewById(R.id.msg_sender_photo);
            msgAttachView = itemView.findViewById(R.id.msg_attachments);
        }

        public void bind(final int position) {
            Message msg = getMessage(position);

            if(msg.getEntityType() == LazyEntity.REAL_ENTITY) {

                if(msg.action == null) {

                    loadAuthorAvatar(position);

                    msgText.setText(msg.text);

                    itemView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if(ctx instanceof ConversationActivity) {
                                itemView.setSelected(!itemView.isSelected());
                                ConversationActivity activity = ((ConversationActivity) ctx);
                                activity.startActionMode(position, itemView.isSelected());
                            }
                            return true;
                        }
                    });

                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(ctx instanceof ConversationActivity)
                                ((ConversationActivity) ctx).getMsgContextMenu(position);
                        }
                    });

                    if(msg.text.length() > 20) {
                        msgTimeRightTv.setVisibility(View.GONE);
                        msgTimeBottomTv.setVisibility(View.VISIBLE);
                    } else {
                        msgTimeRightTv.setVisibility(View.VISIBLE);
                        msgTimeBottomTv.setVisibility(View.GONE);
                    }

                    msgTimeBottomTv.setText(
                            new SimpleDateFormat(" HH:mm ", Locale.getDefault())
                                    .format(new Date(msg.timestamp_long * 1000))
                    );
                    msgTimeRightTv.setText(
                            new SimpleDateFormat(" HH:mm ", Locale.getDefault())
                                    .format(new Date(msg.timestamp_long * 1000))
                    );

                    if(msg.attachments != null && msg.attachments.size() > 0) {

                        if(msg.text.length() == 0)
                            msgText.setVisibility(View.GONE);

                        msgTimeRightTv.setVisibility(View.GONE);
                        msgTimeBottomTv.setVisibility(View.VISIBLE);
                        msgAttachView.loadAttachments(
                                ctx, msg.attachments, imageLoader, msg.author,
                                "chat_attachments"
                        );
                    }

                } else if(msg.author != null && msg.author instanceof User) {

                    User user = (User) msg.author;
                    int stringRes;

                    switch (msg.action.type) {
                        case ChatAction.ACTION_CHAT_CREATE:
                            stringRes =
                                    user.sex == 1 ?
                                            R.string.serv_created_chat_f :
                                            R.string.serv_created_chat_m;
                            msgText.setText(
                                    Html.fromHtml(
                                            ctx.getResources().getString(
                                                stringRes,
                                                String.format("<b>%s</b>", user.first_name),
                                                conv.title
                                        )
                                    )
                            );
                            break;
                        case ChatAction.ACTION_CHAT_PHOTO_UPDATE:
                            stringRes =
                                    user.sex == 1 ?
                                            R.string.chat_photo_updated_f :
                                            R.string.chat_photo_updated_m;

                            msgText.setText(
                                    Html.fromHtml(
                                            ctx.getResources().getString(
                                                    stringRes,
                                                    String.format("<b>%s</b>", user.first_name)
                                            )
                                    )
                            );
                            break;
                        case ChatAction.ACTION_INVITE_USER_BY_LINK:
                            stringRes =
                                    user.sex == 1 ?
                                            R.string.serv_user_invited_by_link_f :
                                            R.string.serv_user_invited_by_link_m;

                            msgText.setText(
                                    Html.fromHtml(
                                            ctx.getResources().getString(
                                                    stringRes,
                                                    String.format("<b>%s</b>", user.first_name)
                                            )
                                    )
                            );
                            break;
                    }
                }
            } else if(msg.getEntityType() == LazyEntity.LAYOUT_HEADER) {
                msgText.setText(
                        Global.formatTimestamp(ctx, msg.timestamp_long * 1000, false)
                );
            }
        }

        private void loadAuthorAvatar(final int position) {

            Message msg = getMessage(position);

            if(!msg.isIncoming)
                return;

            Bitmap bitmap = imageLoader.loadImageSync(
                String.format("file://%s/%s/photos_cache/author_avatars/avatar_%s",
                    ctx.getCacheDir(), instance, msg.author.id
                )
            );

            if(bitmap != null)
                msgAuthorAvatar.setImageBitmap(bitmap);
            else if(msg.author.id > 0 && msg.author.id < Conversation.PEER_ID_USER_UPPER_START){
                bitmap = imageLoader.loadImageSync(
                        String.format("file://%s/%s/photos_cache/profile_avatars/avatar_%s",
                                ctx.getCacheDir(), instance, msg.author.id
                        )
                );
                if(bitmap != null)
                    msgAuthorAvatar.setImageBitmap(bitmap);
            }
        }
    }
}
