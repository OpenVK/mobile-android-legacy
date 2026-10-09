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

package uk.openvk.android.legacy.core.activities.intents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.client.entities.Note;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.client.entities.Video;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.NewPostActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.fragments.VideosFragment;
import uk.openvk.android.legacy.ui.list.items.UploadableAttachment;
import uk.openvk.android.legacy.ui.views.ErrorLayout;
import uk.openvk.android.legacy.ui.views.ProgressLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

@SuppressWarnings("ConstantConditions")
public class VideosIntentActivity extends NetworkFragmentActivity {

    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    public VideosFragment videosFragment;
    private String access_token;
    public User user;
    private String args;
    private int item_pos;
    private int poll_answer;
    private Menu activity_menu;
    private ActionBar actionBar;
    private FragmentTransaction ft;
    private android.support.v7.widget.PopupMenu popup_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        installLayouts();
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        user = new User();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            access_token = mInstancePrefs.getString("access_token", "");
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
        }

        final Uri uri = intent.getData();

        if (uri != null) {
            String path = uri.toString();
            if (mInstancePrefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                mOpenVK.account.getProfileInfo(mOpenVK.wrapper);
                args = Global.getUrlArguments(path);
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        videosFragment.adjustLayout(
                getResources().getConfiguration().orientation
        );
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    @SuppressLint("CommitTransaction")
    private void installLayouts() {
        progressLayout = findViewById(R.id.progress_layout);
        errorLayout = findViewById(R.id.error_layout);
        videosFragment = new VideosFragment();
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_fragment, videosFragment, "videos");
        ft.commit();
        ft = getSupportFragmentManager().beginTransaction();
        ft.show(videosFragment);
        ft.commit();
        progressLayout.enableDarkTheme(true, 0);
        progressLayout.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.videos));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.ic_ab_app);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if(mGlobalPrefs.getString("uiTheme", "blue").equals("Gray")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_gray));
            } else if(mGlobalPrefs.getString("uiTheme", "blue").equals("Black")) {
                getActionBar().setBackgroundDrawable(
                        getResources().getDrawable(R.drawable.bg_actionbar_black));
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAction(new ActionBar.Action() {
                @Override
                public int getDrawable() {
                    return 0;
                }

                @Override
                public void performAction(View view) {
                    onBackPressed();
                }
            });
            actionBar.setTitle(getResources().getString(R.string.videos));
            switch (mGlobalPrefs.getString("uiTheme", "blue")) {
                case "Gray":
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
                    break;
                case "Black":
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar_black));
                    break;
                default:
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
                    break;
            }
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void receiveState(int message, Bundle data) {
        try {
            if(data.containsKey("address")) {
                String activityName = data.getString("address");
                if(activityName == null) {
                    return;
                }
                boolean isCurrentActivity = activityName.equals(
                        String.format("%s_%s", getLocalClassName(), getSessionId())
                );
                if(!isCurrentActivity) {
                    return;
                }
            }
            if(message == HandlerMessages.ACCOUNT_PROFILE_INFO) {
                mOpenVK.videos.getVideos(
                        mOpenVK.wrapper,
                        Long.parseLong(args.substring("videos".length())),
                        25
                );
            } else if (message == HandlerMessages.VIDEOS_GET) {
                progressLayout.setVisibility(View.GONE);
                findViewById(R.id.app_fragment).setVisibility(View.VISIBLE);
                videosFragment.createAdapter(this, mOpenVK.videos.getList());
                videosFragment.setScrollingPositions(this, true);
            } else if (message < 0) {
                try {
                    setErrorPage(data, message);
                } catch (Exception ex) {
                    setErrorPage(data, HandlerMessages.INVALID_JSON_RESPONSE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            setErrorPage(data, HandlerMessages.INVALID_JSON_RESPONSE);
        }
    }

    private void setErrorPage(Bundle data, int reason) {
        findViewById(R.id.app_fragment).setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
        errorLayout.setData(data);
        errorLayout.setRetryAction(mOpenVK.wrapper, mOpenVK.account);
        errorLayout.setReason(reason);
        errorLayout.setProgressLayout(progressLayout);
        errorLayout.setTitle(getResources().getString(R.string.err_text));
        progressLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    public void pickVideo(int position) {
        Intent intent = getIntent();
        Video video = mOpenVK.videos.getList().get(position);
        intent.putExtra("attachment", String.format("video%s_%s", video.owner_id, video.id));
        intent.putExtra("video_id", video.id);
        intent.putExtra("owner_id", video.owner_id);
        intent.putExtra("video_title", video.title);
        setResult(UploadableAttachment.Result.RESULT_ATTACH_VIDEO, intent);
        finish();
    }
}