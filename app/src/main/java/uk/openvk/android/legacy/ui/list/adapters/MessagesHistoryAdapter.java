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
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.client.entities.ChatAction;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.client.entities.Message;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.views.base.TightTextView;

public class MessagesHistoryAdapter extends RecyclerView.Adapter<MessagesHistoryAdapter.Holder> {
    private final Context ctx;
    private final Conversation conv;
    private ArrayList<Message> history;

    public MessagesHistoryAdapter(Context ctx, Conversation conv, ArrayList<Message> history) {
        this.conv = conv;
        this.ctx = ctx;
        this.history = history;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutRes;

        switch (viewType) {
            case 0x81:  // if is incoming message
                layoutRes = R.layout.list_item_msg_incoming;
                break;
            case 0x80:  // if is outcoming message
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

    private Message getMessage(int position) {
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
        else
            return msg.isIncoming ? 0x81 : 0x80;
    }

    public class Holder extends RecyclerView.ViewHolder {

        private final ProgressBar progressBar;
        private final ImageView msgErrorIcon;
        private final TightTextView msgText;
        private final TextView msgTimeRightTv;
        private final TextView msgTimeBottomTv;

        public Holder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.msg_progress);
            msgErrorIcon = itemView.findViewById(R.id.msg_failed);
            msgText = itemView.findViewById(R.id.msg_text);
            msgTimeRightTv = itemView.findViewById(R.id.msg_time_right);
            msgTimeBottomTv = itemView.findViewById(R.id.msg_time_bottom);
        }

        public void bind(int position) {
            Message msg = getMessage(position);

            if(msg.action == null)
                msgText.setText(msg.text);
            else {
                if(msg.author != null && msg.author instanceof User) {

                    User user = (User) msg.author;
                    int stringRes;

                    switch (msg.action.type) {
                        case ChatAction.ACTION_CHAT_CREATE:
                            stringRes =
                                    user.sex == 1 ?
                                            R.string.serv_created_chat_f :
                                            R.string.serv_created_chat_m;
                            msgText.setText(
                                    ctx.getResources().getString(
                                            stringRes, user.first_name,
                                            conv.title
                                    )
                            );
                            break;
                        case ChatAction.ACTION_CHAT_PHOTO_UPDATE:
                            stringRes =
                                    user.sex == 1 ?
                                            R.string.chat_photo_updated_f :
                                            R.string.chat_photo_updated_m;

                            msgText.setText(
                                    ctx.getResources().getString(
                                            stringRes, user.first_name
                                    )
                            );
                            break;
                        case ChatAction.ACTION_INVITE_USER_BY_LINK:
                            stringRes =
                                    user.sex == 1 ?
                                            R.string.serv_user_invited_by_link_f :
                                            R.string.serv_user_invited_by_link_m;

                            msgText.setText(
                                    ctx.getResources().getString(
                                            stringRes, user.first_name
                                    )
                            );
                            break;
                    }
                }
            }
        }
    }
}
