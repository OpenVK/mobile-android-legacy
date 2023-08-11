package uk.openvk.android.legacy.ui.core.fragments.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Friend;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.ui.list.adapters.UsersListAdapter;

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

public class UsersFragment extends Fragment {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_prefs;
    private ListView usersListView;
    private ArrayList<User> users;
    private ArrayList<Friend> requests;
    private UsersListAdapter usersAdapter;
    public boolean loading_more_friends;
    private View view;
    private Context activity_ctx;
    private String instance;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_users, container, false);
        usersListView = view.findViewById(R.id.users_listview);
        //TabHost users_tabhost = view.findViewById(R.id.users_tabhost);
        if(activity_ctx == null) {
            activity_ctx = getActivity();
        }
/*
        [NOT IMPLEMENTED YET!]
        if(activity_ctx.getClass().getSimpleName().equals("AppActivity")) {
            ((TabSelector) view.findViewById(R.id.selector)).setLength(2);
            setupTabHost(users_tabhost, "members");
        } else {
            ((TabSelector) view.findViewById(R.id.selector)).setLength(1);
            setupTabHost(users_tabhost, "admins");
        }
        ((TabSelector) view.findViewById(R.id.selector)).setTabTitle(0, getResources().getString(R.string.group_members));
        ((TabSelector) view.findViewById(R.id.selector)).setTabTitle(1, getResources().getString(R.string.group_admins));
        ((TabSelector) view.findViewById(R.id.selector)).setup(friends_tabhost, new
            View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
        });
*/
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<User> users) {
        if(view != null) {
            this.users = users;
            if (usersAdapter == null) {
                usersAdapter = new UsersListAdapter(ctx, this, users);
                usersListView.setAdapter(usersAdapter);
            } else {
                usersAdapter.notifyDataSetChanged();
            }
        }
    }

    public int getCount() {
        try {
            return usersAdapter.getCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void loadAvatars() {
        if(usersAdapter != null) {
            usersListView = view.findViewById(R.id.users_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    User item = users.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/users_avatars/avatar_%s",
                                    getContext().getCacheDir(), instance, item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    users.set(i, item);
                } catch (OutOfMemoryError | Exception ex) {
                    ex.printStackTrace();
                }
            }
            usersAdapter.notifyDataSetChanged();
        }
        if(requests != null) {
            for (int i = 0; i < requests.size(); i++) {
                try {
                    Friend item = requests.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile
                            (String.format("%s/%s/photos_cache/users_avatars/avatar_%s",
                                    getContext().getCacheDir(), instance, item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    } else {
                        Log.e(OvkApplication.APP_TAG,
                                String.format("%s/%s/photos_cache/users_avatars/avatar_%d",
                                        getContext().getCacheDir(), instance, item.id));
                    }
                    requests.set(i, item);
                } catch (OutOfMemoryError | Exception ex) {
                    ex.printStackTrace();
                }
            }
            usersAdapter.notifyDataSetChanged();
        }
    }

    public void refresh() {
        if(usersAdapter != null) {
            usersAdapter.notifyDataSetChanged();
        }
        if(usersAdapter != null) {
            usersAdapter.notifyDataSetChanged();
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        loading_more_friends = false;
        usersListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if(infinity_scroll) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount) {
                        if(!loading_more_friends) {
                            if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                                loading_more_friends = true;
                                ((AppActivity) ctx).loadMoreFriends();
                            } else if(ctx.getClass().getSimpleName()
                                    .equals("FriendsIntentActivity")) {
                                loading_more_friends = true;
                                ((FriendsIntentActivity) ctx).loadMoreFriends();
                            }
                        }
                    }
                }
            }
        });
    }

    private void setupTabHost(TabHost tabhost, String where) {
//        tabhost.setup();
//        if (where.equals("members")) {
//            TabHost.TabSpec tabSpec = tabhost.newTabSpec("main");
//            tabSpec.setContent(R.id.tab1);
//            tabSpec.setIndicator(getResources().getString(R.string.friends));
//            tabhost.addTab(tabSpec);
//        } else if (where.equals("friends_2")) {
//            TabHost.TabSpec tabSpec = tabhost.newTabSpec("main");
//            tabSpec.setContent(R.id.tab1);
//            tabSpec.setIndicator(getResources().getString(R.string.friends));
//            tabhost.addTab(tabSpec);
//            tabSpec = tabhost.newTabSpec("requests");
//            tabSpec.setContent(R.id.tab2);
//            tabSpec.setIndicator(getResources().getString(R.string.friend_requests));
//            tabhost.addTab(tabSpec);
//        }
    }

    public void setActivityContext(Context ctx) {
        activity_ctx = ctx;
    }

    public void hideSelectedItemBackground(int position) {
        (view.findViewById(R.id.friends_listview)).setBackgroundColor(
                getResources().getColor(R.color.transparent));
    }
}
