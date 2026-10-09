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

package uk.openvk.android.legacy.core.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.Account;
import uk.openvk.android.client.entities.Conversation;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.ui.list.adapters.ConversationsListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;

public class ConversationsFragment extends ActiveFragment {
    private ArrayList<Conversation> conversations;
    private ConversationsListAdapter conversationsAdapter;
    private RecyclerView convListView;
    private Account account;
    private View view;
    private String instance;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversations, container, false);
        convListView = view.findViewById(R.id.conversations_listview);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public void createAdapter() {
        this.conversations = mOpenVK.messages.getConversations();
        this.account = mOpenVK.account;
        conversationsAdapter = new ConversationsListAdapter(getActivity(), this.conversations, account);

        int orientation = getResources().getConfiguration().orientation;
        adjustLayoutSize(getActivity(), orientation);
        convListView.setAdapter(conversationsAdapter);
    }

    public void adjustLayoutSize(Context ctx, int orientation) {
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        if(app.isTablet && app.swdp >= 600) {
            LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
            glm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.conversations_listview)).setLayoutManager(glm);
        } else {
            LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            ((RecyclerView) view.findViewById(R.id.conversations_listview)).setLayoutManager(llm);
        }
    }

    public int getCount() {
        if(convListView.getAdapter() != null) {
            return convListView.getAdapter().getItemCount();
        } else {
            return 0;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //createAdapter(getContext(), conversations, account);
        adjustLayoutSize(getContext(), newConfig.orientation);
    }

    @Override
    public int getObjectsSize() {
        return conversations != null ? conversations.size() : 0;
    }

    public void refresh() {
        if(conversationsAdapter != null)
            conversationsAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onReceivedAPIResponse(int message, Bundle data) {
        super.onReceivedAPIResponse(message, data);

        if(getView() == null)
            return false;

        switch (message) {
            case HandlerMessages.MESSAGES_CONVERSATIONS:
            case HandlerMessages.CONVERSATIONS_AVATARS:
                createAdapter();
                break;
        }

        return true;
    }
}
