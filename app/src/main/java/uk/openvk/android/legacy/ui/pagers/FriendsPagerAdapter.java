package uk.openvk.android.legacy.ui.pagers;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;

import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Friend;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.fragments.friends.FriendListFragment;

public class FriendsPagerAdapter extends FragmentStatePagerAdapter {

    private final long userId;
    private final long accountId;
    private long friendsTotal;
    private long onlineTotal;
    private long requestsTotal;
    public ArrayList<ActiveFragment> fragments;
    private Context ctx;

    public FriendsPagerAdapter(Context ctx, FragmentManager fm,
                               long userId, long accountId,
                               long friendsTotal, long requestsTotal) {
        super(fm);
        this.ctx = ctx;
        fragments = new ArrayList<>();

        fragments.add(FriendListFragment.createInstance(0));

        this.userId = userId;
        this.accountId = accountId;

        this.friendsTotal = friendsTotal;
        this.requestsTotal = requestsTotal;

        if(accountId == userId)
            fragments.add(FriendListFragment.createInstance(1));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    public int getFragmentObjectsSize(int position) {
        Fragment fragment = getItem(position);
        return fragment != null && fragment instanceof ActiveFragment ?
                ((ActiveFragment) fragment).getObjectsSize() : 0;
    }

    public void createListAdapter(OpenVKAPI ovk_api, int position, ArrayList<Friend> friends) {
        Fragment fragment = getItem(position);

        if(fragment != null && fragment instanceof FriendListFragment) {
            if(((FriendListFragment) fragment).getObjectsSize() == -1) {
                ((FriendListFragment) fragment).createAdapter(
                        ctx, userId, ovk_api, friends
                );
            }
        }

        fragments.set(position, ((ActiveFragment) fragment));
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            default:
                return Global.getPluralQuantityString(
                        ctx, R.plurals.friends_tab_all, friendsTotal
                );
            case 1:
                return String.format(
                        "%s (%s)",
                        ctx.getResources().getString(R.string.friend_requests),
                        requestsTotal
                );
        }
    }
}
