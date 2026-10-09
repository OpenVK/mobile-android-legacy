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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.Locale;

import dev.tinelix.retro_ab.ActionBar;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.OpenVKAPI;
import uk.openvk.android.client.entities.Note;
import uk.openvk.android.client.enumerations.HandlerMessages;
import uk.openvk.android.client.models.Notes;
import uk.openvk.android.client.models.Users;
import uk.openvk.android.legacy.core.activities.NewPostActivity;
import uk.openvk.android.legacy.core.activities.base.NetworkFragmentActivity;
import uk.openvk.android.legacy.core.activities.base.TranslucentFragmentActivity;
import uk.openvk.android.legacy.core.fragments.NotesFragment;
import uk.openvk.android.legacy.ui.list.items.SlidingMenuObject;
import uk.openvk.android.legacy.ui.list.items.UploadableAttachment;
import uk.openvk.android.legacy.ui.views.ErrorLayout;
import uk.openvk.android.legacy.ui.views.ProgressLayout;
import uk.openvk.android.legacy.ui.wrappers.LocaleContextWrapper;

public class NotesIntentActivity extends NetworkFragmentActivity {

    public Handler handler;
    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private NotesFragment notesFragment;
    private long user_id = 0;
    private FragmentTransaction ft;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        installLayouts();
        Intent intent = getIntent();
        final Uri uri = intent.getData();

        if (uri != null) {
            String path = uri.toString();
            try {
                String args = Global.getUrlArguments(path);
                if(args.length() > 0) {
                    if(args.startsWith("id")) {
                        try {
                            user_id = Integer.parseInt(args.substring(2));
                            mOpenVK.account.getProfileInfo(mOpenVK.wrapper);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
                return;
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        Locale languageType = OvkApplication.getLocale(newBase);
        super.attachBaseContext(LocaleContextWrapper.wrap(newBase, languageType));
    }

    @SuppressWarnings("ConstantConditions")
    private void installLayouts() {
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        notesFragment = new NotesFragment();

        progressLayout.setVisibility(View.VISIBLE);
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_fragment, notesFragment, "notes");
        ft.commit();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.notes));
                    if(mGlobalPrefs.getString("uiTheme", "blue").equals("Gray")) {
                        getActionBar().setBackgroundDrawable(
                                getResources().getDrawable(R.drawable.bg_actionbar_gray));
                    } else if(mGlobalPrefs.getString("uiTheme", "blue").equals("Black")) {
                        getActionBar().setBackgroundDrawable(
                                getResources().getDrawable(R.drawable.bg_actionbar_black));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.ic_ab_app);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            final ActionBar actionBar = findViewById(R.id.actionbar);
            actionBar.setHomeLogo(R.drawable.ic_ab_app);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_actionbar));
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
            actionBar.setTitle(getResources().getString(R.string.notes));

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
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    public void receiveState(int message, Bundle data) {
        try {
            if (message < 0) {
                setErrorPage(data, "ovk", message, true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setErrorPage(Bundle data, String icon, int reason, boolean showRetry) {
        progressLayout.setVisibility(View.GONE);
        findViewById(R.id.app_fragment).setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorLayout.setReason(HandlerMessages.INVALID_JSON_RESPONSE);
        errorLayout.setIcon(icon);
        errorLayout.setData(data);
        errorLayout.setRetryAction(mOpenVK.wrapper, mOpenVK.account);
        errorLayout.setReason(reason);
        if (icon.equals("ovk")) {
            if(reason == HandlerMessages.NOTES_GET) {
                errorLayout.setTitle(
                        getResources().getString(R.string.no_notes));
            }
        } else {
            errorLayout.setTitle(getResources().getString(R.string.err_text));
        }
        if (!showRetry) {
            errorLayout.hideRetryButton();
        }
        progressLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
    }

    public void pickNote(int position) {
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        Note note = mOpenVK.notes.list.get(position);
        intent.putExtra("attachment", String.format("note%s_%s", note.owner_id, note.id));
        intent.putExtra("note_id", note.id);
        intent.putExtra("owner_id", note.owner_id);
        intent.putExtra("note_title", note.title);
        intent.putExtra("note_content", note.content);
        setResult(UploadableAttachment.Result.RESULT_ATTACH_NOTE, intent);
        finish();
    }
}
