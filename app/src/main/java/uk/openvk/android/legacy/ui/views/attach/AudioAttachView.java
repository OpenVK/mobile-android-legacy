package uk.openvk.android.legacy.ui.views.attach;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.core.fragments.AudiosFragment;
import uk.openvk.android.legacy.services.AudioPlayerService;
import uk.openvk.android.legacy.ui.views.WallLayout;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

public class AudioAttachView extends FrameLayout {
    private final String instance;
    private Attachment attachment;
    private Bitmap thumbnail;
    private boolean isBoundAP;
    private Intent serviceIntent;
    private int status;

    public AudioAttachView(@NonNull Context context) {
        super(context);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.attach_audio, null);
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        this.addView(view);
        view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public AudioAttachView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.attach_audio, null);
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        this.addView(view);
        view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @SuppressLint("DefaultLocale")
    public void setAttachment(final Context ctx,
                              final int position,
                              final long post_id,
                              Attachment attachment) {
        this.attachment = attachment;
        if (attachment != null) {
            if (attachment.type.equals("audio")) {
                final Audio audioAttachment = ((Audio) attachment);
                ((TextView) findViewById(R.id.attach_title)).setText(audioAttachment.title);
                ((TextView) findViewById(R.id.attach_subtitle)).setText(audioAttachment.artist);
                ((TextView) findViewById(R.id.attach_duration)).setText(audioAttachment.getDuration());
                status = 0;
                findViewById(R.id.attach_icon).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AudiosFragment fragment = new AudiosFragment();
                        startAudioPlayerService();
                        switch (status) {
                            default:
                                WallLayout wallLayout = null;
                                if(ctx instanceof AppActivity) {
                                    wallLayout = ((AppActivity) ctx).profilePageFragment.wallLayout;
                                } else if(ctx instanceof ProfileIntentActivity) {
                                    wallLayout = ((ProfileIntentActivity) ctx).profilePageFragment.wallLayout;
                                } else if(ctx instanceof GroupIntentActivity) {
                                    wallLayout = ((GroupIntentActivity) ctx).wallLayout;
                                }
                                if(wallLayout != null) {
                                    wallLayout.setAudioPlayerState(AudioPlayerService.STATUS_STARTING_FROM_WALL, position, post_id);
                                    ((ImageView) findViewById(R.id.attach_icon)).setImageDrawable(
                                            getResources().getDrawable(R.drawable.attach_audio_pause)
                                    );
                                    status = 1;
                                }
                                break;
                            case 1:
                                wallLayout = null;
                                if(ctx instanceof AppActivity) {
                                    wallLayout = ((AppActivity) ctx).profilePageFragment.wallLayout;
                                } else if(ctx instanceof ProfileIntentActivity) {
                                    wallLayout = ((ProfileIntentActivity) ctx).profilePageFragment.wallLayout;
                                } else if(ctx instanceof GroupIntentActivity) {
                                    wallLayout = ((GroupIntentActivity) ctx).wallLayout;
                                }
                                if(wallLayout != null) {
                                    wallLayout.setAudioPlayerState(AudioPlayerService.STATUS_PAUSED, position, post_id);
                                    ((ImageView) findViewById(R.id.attach_icon)).setImageDrawable(
                                            getResources().getDrawable(R.drawable.attach_audio_play)
                                    );
                                    status = 2;
                                }
                                break;
                            case 2:
                                wallLayout = null;
                                if(ctx instanceof AppActivity) {
                                    wallLayout = ((AppActivity) ctx).profilePageFragment.wallLayout;
                                } else if(ctx instanceof ProfileIntentActivity) {
                                    wallLayout = ((ProfileIntentActivity) ctx).profilePageFragment.wallLayout;
                                } else if(ctx instanceof GroupIntentActivity) {
                                    wallLayout = ((GroupIntentActivity) ctx).wallLayout;
                                }
                                if(wallLayout != null) {
                                    wallLayout.setAudioPlayerState(AudioPlayerService.STATUS_PLAYING, position, post_id);
                                    ((ImageView) findViewById(R.id.attach_icon)).setImageDrawable(
                                            getResources().getDrawable(R.drawable.attach_audio_pause)
                                    );
                                    status = 1;
                                }
                                break;
                        }
                    }
                });
            }
        }
    }

    private void startAudioPlayerService() {
        serviceIntent = new Intent(getContext().getApplicationContext(), AudioPlayerService.class);
        if(!isBoundAP) {
            OvkApplication app = ((OvkApplication) getContext().getApplicationContext());
            Log.d(OvkApplication.APP_TAG, "Creating AudioPlayerService intent");
            serviceIntent.putExtra("action", "PLAYER_CREATE");
        } else {
            serviceIntent.putExtra("action", "PLAYER_GET_CURRENT_POSITION");
        }
        getContext().getApplicationContext().startService(serviceIntent);
    }
}
