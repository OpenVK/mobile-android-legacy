package uk.openvk.android.legacy.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.base.ActiviableFragment;

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
            if(appActivity.selectedFragment instanceof ActiviableFragment) {
                ((ActiviableFragment) appActivity.selectedFragment).onDeactivated();
            }
            switch (where) {
                case "profile":
                    ft.show(appActivity.profileFragment);
                    appActivity.selectedFragment = appActivity.profileFragment;
                    if(appActivity.profile_loaded) {
                        showFragment(activity, appActivity.ovk_api.user.first_name != null);
                    }
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "profile");
                    break;
                case "friends":
                    ft.show(appActivity.friendsFragment);
                    showFragment(activity, appActivity.friendsFragment.getCount() != 0);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.selectedFragment = appActivity.friendsFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "friends");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        appActivity.actionBar.removeAllActions();
                    } else {
                        if(appActivity.activity_menu != null) {
                            appActivity.activity_menu.clear();
                        }
                    }
                    break;
                case "photos":
                    ft.show(appActivity.photosFragment);
                    showFragment(activity, appActivity.photosFragment.getCount() != 0);
                    appActivity.progressLayout.enableDarkTheme(true);
                    appActivity.selectedFragment = appActivity.photosFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "photos");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        appActivity.actionBar.removeAllActions();
                    } else {
                        if(appActivity.activity_menu != null) {
                            appActivity.activity_menu.clear();
                        }
                    }
                    break;
                case "videos":
                    ft.show(appActivity.videosFragment);
                    showFragment(activity, appActivity.videosFragment.getCount() != 0);
                    appActivity.progressLayout.enableDarkTheme(true);
                    appActivity.selectedFragment = appActivity.videosFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "videos");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        appActivity.actionBar.removeAllActions();
                    } else {
                        if(appActivity.activity_menu != null) {
                            appActivity.activity_menu.clear();
                        }
                    }
                    break;
                case "audios":
                    ft.show(appActivity.audiosFragment);
                    showFragment(activity, appActivity.videosFragment.getCount() != 0);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.selectedFragment = appActivity.audiosFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "audios");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        appActivity.actionBar.removeAllActions();
                    } else {
                        if(appActivity.activity_menu != null) {
                            appActivity.activity_menu.clear();
                        }
                    }
                    break;
                case "messages":
                    ft.show(appActivity.conversationsFragment);
                    showFragment(activity, appActivity.conversationsFragment.getCount() != 0);
                    appActivity.selectedFragment = appActivity.conversationsFragment;
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.global_prefs_editor.putString("current_screen", "messages");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        appActivity.actionBar.removeAllActions();
                    } else {
                        if(appActivity.activity_menu != null) {
                            appActivity.activity_menu.clear();
                        }
                    }
                    break;
                case "groups":
                    ft.show(appActivity.groupsFragment);
                    showFragment(activity, appActivity.groupsFragment.getCount() != 0);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.selectedFragment = appActivity.groupsFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "groups");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        appActivity.actionBar.removeAllActions();
                    } else {
                        if(appActivity.activity_menu != null) {
                            appActivity.activity_menu.clear();
                        }
                    }
                    break;
                case "notes":
                    ft.show(appActivity.notesFragment);
                    showFragment(activity, appActivity.notesFragment.getCount() != 0);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.selectedFragment = appActivity.notesFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "notes");
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                        appActivity.actionBar.removeAllActions();
                    } else {
                        if(appActivity.activity_menu != null) {
                            appActivity.activity_menu.clear();
                        }
                    }
                    break;
                case "newsfeed":
                    ft.show(appActivity.newsfeedFragment);
                    showFragment(activity, appActivity.newsfeedFragment.getCount() != 0);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.selectedFragment = appActivity.newsfeedFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "newsfeed");
                    appActivity.setActionBar("custom_newsfeed");
                    break;
                case "settings":
                    ft.show(appActivity.mainSettingsFragment);
                    showFragment(activity, true);
                    appActivity.progressLayout.enableDarkTheme(false);
                    appActivity.selectedFragment = appActivity.mainSettingsFragment;
                    appActivity.global_prefs_editor.putString("current_screen", "settings");
                    break;
            }
            ft.commit();
            appActivity.global_prefs_editor.commit();
            if(appActivity.selectedFragment instanceof ActiviableFragment) {
                ((ActiviableFragment) appActivity.selectedFragment).onActivated();
            }
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
