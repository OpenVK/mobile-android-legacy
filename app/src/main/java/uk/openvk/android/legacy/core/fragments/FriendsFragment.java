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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.ui.pagers.FriendsPagerAdapter;

public class FriendsFragment extends ActiveFragment {
    public String state;
    private View view;
    private ViewPager pager;
    private FriendsPagerAdapter pagerAdapter;
    private Context activity_ctx;
    private String instance;
    private PagerSlidingTabStrip pagerTabs;
    private ArrayList<Friend> friendsList;
    private SharedPreferences globalPrefs;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);
        pager = view.findViewById(R.id.friends_pager);
        pagerTabs = view.findViewById(R.id.pagerTabStrip);

        if(activity_ctx == null) activity_ctx = getActivity();

        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();

        globalPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String uiTheme = globalPrefs.getString("uiTheme", "blue");

        switch (uiTheme) {
            case "Blue":
            case "blue":
                pagerTabs.setIndicatorColor(getResources().getColor(R.color.tab_indicator));
                break;
        }

        if(!((OvkApplication) getContext().getApplicationContext()).isTablet)
            pagerTabs.setShouldExpand(true);

        return view;
    }

    public int getCount() {
        return this.friendsList.size();
    }

    public void refresh() {

    }

    public void setActivityContext(Context ctx) {
        activity_ctx = ctx;
    }

    public void loadAPIData(Context ctx, long userId) {

        if(userId == 0)
            userId = mOpenVK.account.id;

        if(pagerAdapter != null) {
            pagerAdapter.createListAdapter(mOpenVK, 0, mOpenVK.friends.getFriends());
            return;
        }

        pagerAdapter = new FriendsPagerAdapter(
                ctx, getFragmentManager(), userId, mOpenVK.account.id,
                mOpenVK.friends.count,
                mOpenVK.account.counters.friends_requests
        );

        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(2);

        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(positionOffset == 0.0f || positionOffset == 0.8f) {
                    if(position == 0)
                        pagerAdapter.createListAdapter(mOpenVK, position, mOpenVK.friends.getFriends());
                    else if(position == 1)
                        pagerAdapter.createListAdapter(mOpenVK, position, mOpenVK.friends.requests);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if(position == 0)
                    pagerAdapter.createListAdapter(mOpenVK, position, mOpenVK.friends.getFriends());
                else if(position == 1)
                    pagerAdapter.createListAdapter(mOpenVK, position, mOpenVK.friends.requests);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        pagerTabs.setViewPager(pager);
        pagerTabs.setOnPageChangeListener(listener);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public int getObjectsSize() {
        return friendsList != null ? friendsList.size() : 0;
    }

    public void updateFriendsAdapters() {
    }

    public ViewPager getViewPager() {
        return pager;
    }

    @Override
    public boolean onReceivedAPIResponse(int message, Bundle data) {
        super.onReceivedAPIResponse(message, data);

        if(getView() == null)
            return false;

        switch (message) {
            case HandlerMessages.FRIENDS_GET:
            case HandlerMessages.FRIENDS_GET_MORE:
            case HandlerMessages.FRIENDS_REQUESTS:
                loadAPIData(getActivity(), 0);
                break;
            case HandlerMessages.FRIEND_AVATARS:
                refresh();
                break;
        }

        return true;
    }
}
