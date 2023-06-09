package uk.openvk.android.legacy.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.ProfileIntentActivity;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class FragmentNavigator {
    private Activity activity;

    public FragmentNavigator(Context ctx) {
        if(ctx instanceof AppActivity) {
            activity = ((AppActivity) ctx);
        } else if(ctx instanceof ProfileIntentActivity) {
            activity = ((ProfileIntentActivity) ctx);
        } else if(ctx instanceof GroupIntentActivity) {
            activity = ((GroupIntentActivity) ctx);
        }
    }
    public void navigateTo(String where, FragmentTransaction ft) {
        if(activity instanceof AppActivity) {
            AppActivity appActivity = ((AppActivity) activity);
            appActivity.errorLayout.setVisibility(View.GONE);
            ft.hide(appActivity.selectedFragment);
            switch (where) {
                case "friends":
                    ft.show(appActivity.friendsFragment);
                    showFragment(activity, appActivity.friendsFragment.getCount() != 0);
                    appActivity.selectedFragment = appActivity.friendsFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "friends");
                    break;
                case "messages":
                    ft.show(appActivity.conversationsFragment);
                    showFragment(activity, appActivity.conversationsFragment.getCount() != 0);
                    appActivity.selectedFragment = appActivity.conversationsFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "messages");
                    break;
                case "groups":
                    ft.show(appActivity.groupsFragment);
                    showFragment(activity, appActivity.groupsFragment.getCount() != 0);
                    appActivity.selectedFragment = appActivity.conversationsFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "messages");
                    break;
                case "newsfeed":
                    ft.show(appActivity.newsfeedFragment);
                    showFragment(activity, appActivity.newsfeedFragment.getCount() != 0);
                    appActivity.selectedFragment = appActivity.newsfeedFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "newsfeed");
                    break;
                case "settings":
                    ft.show(appActivity.mainSettingsFragment);
                    showFragment(activity, true);
                    appActivity.selectedFragment = appActivity.mainSettingsFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "settings");
                    break;
            }
            ft.commit();
            appActivity.global_prefs_editor.commit();
        }
    }

    private void showFragment(Activity activity, boolean status) {
        if(activity instanceof AppActivity) {
            AppActivity appActivity = ((AppActivity) activity);
            if(status) {
                appActivity.findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                appActivity.progressLayout.setVisibility(View.GONE);
            } else {
                appActivity.findViewById(R.id.app_fragment).setVisibility(View.GONE);
                appActivity.progressLayout.setVisibility(View.VISIBLE);
            }
        }
    }
}
