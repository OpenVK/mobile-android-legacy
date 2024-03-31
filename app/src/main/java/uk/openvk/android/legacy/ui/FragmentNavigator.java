/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.legacy.ui;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.AudiosFragment;
import uk.openvk.android.legacy.core.fragments.ConversationsFragment;
import uk.openvk.android.legacy.core.fragments.FriendsFragment;
import uk.openvk.android.legacy.core.fragments.GroupsFragment;
import uk.openvk.android.legacy.core.fragments.MainSettingsFragment;
import uk.openvk.android.legacy.core.fragments.NewsfeedFragment;
import uk.openvk.android.legacy.core.fragments.NotesFragment;
import uk.openvk.android.legacy.core.fragments.PhotosFragment;
import uk.openvk.android.legacy.core.fragments.VideosFragment;
import uk.openvk.android.legacy.core.fragments.base.ActiveFragment;
import uk.openvk.android.legacy.core.fragments.pages.ProfilePageFragment;

public class FragmentNavigator {
    private Activity activity;

    public FragmentNavigator(Context ctx) {
        if(ctx instanceof NetworkFragmentActivity) {
            activity = ((NetworkFragmentActivity) ctx);
        }
    }

    public void navigateTo(String where, FragmentTransaction ft) {
        if(activity instanceof AppActivity) {
            final AppActivity appActivity = ((AppActivity) activity);
            appActivity.errorLayout.setVisibility(View.GONE);
            appActivity.progressLayout.setVisibility(View.VISIBLE);
            ft.hide(appActivity.selectedFragment);
            if(appActivity.selectedFragment instanceof ActiveFragment) {
                ((ActiveFragment) appActivity.selectedFragment).onDeactivated();
            }
            showFragment(activity, true);
            switch (where) {
                case "profile":
                    appActivity.selectedFragment = new ProfilePageFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "profile");
                    break;
                case "friends":
                    appActivity.selectedFragment = new FriendsFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "friends");
                    break;
                case "photos":
                    appActivity.selectedFragment = new PhotosFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "photos");
                    break;
                case "videos":
                    appActivity.selectedFragment = new VideosFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "videos");
                    break;
                case "audios":
                    appActivity.selectedFragment = new AudiosFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "audios");
                    break;
                case "messages":
                    appActivity.selectedFragment = new ConversationsFragment();
                    showFragment(activity, true);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "conversations");
                    break;
                case "groups":
                    appActivity.selectedFragment = new GroupsFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "groups");
                    break;
                case "notes":
                    appActivity.selectedFragment = new NotesFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "groups");
                    break;
                case "newsfeed":
                    appActivity.selectedFragment = new NewsfeedFragment();
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "newsfeed");
                    appActivity.setActionBar("custom_newsfeed");
                    break;
                case "settings":
                    appActivity.selectedFragment = new MainSettingsFragment();
                    showFragment(activity, true);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "settings");
                    break;
            }
            ft.commit();
            appActivity.global_prefs_editor.commit();
            if(appActivity.selectedFragment instanceof ActiveFragment) {
                ((ActiveFragment) appActivity.selectedFragment).onActivated();
            }
            ft.replace(R.id.app_fragment, appActivity.selectedFragment);
        }
    }

    private void showFragment(Activity activity, final boolean status) {
        if(activity instanceof AppActivity) {
            final AppActivity appActivity = ((AppActivity) activity);
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
