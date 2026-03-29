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

package uk.openvk.android.legacy.ui.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.attachments.Attachment;
import uk.openvk.android.client.entities.Audio;
import uk.openvk.android.client.entities.Photo;
import uk.openvk.android.legacy.services.AudioPlayerService;
import uk.openvk.android.legacy.ui.list.adapters.NewsfeedAdapter;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;
import uk.openvk.android.legacy.ui.views.attach.AudioAttachView;
import uk.openvk.android.legacy.ui.views.base.InfinityRecyclerView;
import uk.openvk.android.legacy.ui.views.base.SmoothRecyclerView;

public class WallLayout extends LinearLayout {
    private final String instance;
    private View headerView;
    private int param = 0;
    public TextView titlebar_title;
    public String state;
    public JSONArray wall;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private NewsfeedAdapter wallAdapter;
    private RecyclerView wallView;
    private LinearLayoutManager llm;
    private ArrayList<WallPost> wallItems;
    public boolean isActivatedAP;

    public WallLayout(Context context) {
        super(context);
        View view =  new InfinityRecyclerView(getContext());
        this.addView(view);

        view.setLayoutParams(
                new LayoutParams(
                        getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ?
                                480 : LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        setGravity(Gravity.CENTER);

        llm = new WrappedLinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        int orientation = ((OvkApplication) getContext().getApplicationContext()).config.orientation;

        adjustLayoutSize(orientation);
        instance = PreferenceManager.getDefaultSharedPreferences(
                getContext()).getString("current_instance", "");
    }

    public WallLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  new SmoothRecyclerView(getContext());
        this.addView(view);
        setGravity(Gravity.CENTER);

        ViewCompat.setNestedScrollingEnabled(view, false);
        ((RecyclerView) view).setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setFocusableInTouchMode(true);

        llm = new WrappedLinearLayoutManager(context) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        int orientation = ((OvkApplication) getContext().getApplicationContext()).config.orientation;

        adjustLayoutSize(orientation);
        instance = PreferenceManager.getDefaultSharedPreferences(
                getContext()).getString("current_instance", "");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        try {
            if (((OvkApplication) getContext().getApplicationContext()).isTablet) {
                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        requestLayout();
                        adjustLayoutSize(getContext().getResources().getConfiguration()
                                .orientation);
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createAdapter(Context ctx, ArrayList<WallPost> wallItems) {
        this.wallItems = wallItems;
        wallAdapter = new NewsfeedAdapter(ctx, wallItems, true);
        if(getChildCount() > 0)
            wallView = (RecyclerView) getChildAt(0);

        if(wallView == null)
            return;

        llm.setOrientation(LinearLayoutManager.VERTICAL);
        wallView.setLayoutManager(llm);
        wallView.setAdapter(wallAdapter);
    }

    public void updateItem(WallPost item, int position) {
        if(wallAdapter != null) {
            if(getChildCount() > 0)
                wallView = (RecyclerView) getChildAt(0);

            if(wallView == null)
                return;
            wallItems.set(position, item);
            wallAdapter.notifyItemChanged(position);
        }
    }

    public void loadPhotos() {
        if(getChildCount() > 0)
            wallView = (RecyclerView) getChildAt(0);

        if(wallView == null)
            return;
        try {
            if(wallAdapter != null) {
                int visibleItemCount = llm.getChildCount();
                int totalItemCount = llm.getItemCount();
                int firstVisibleItemPosition = llm.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = llm.findLastVisibleItemPosition();
                for (int i = 0; i < totalItemCount; i++) {
                    WallPost item = wallItems.get(i);
                    try {
                        if(item.repost != null) {
                            if (item.repost.newsfeed_item.attachments.size() > 0) {
                                if (item.repost.newsfeed_item.attachments.get(0).type.equals("photo")) {
                                    Photo photo = ((Photo) item.repost.newsfeed_item.attachments.get(0));
                                    Attachment attachment = item.repost.newsfeed_item.attachments.get(0);
                                    if (i < firstVisibleItemPosition || i > lastVisibleItemPosition) {
                                        if(photo.bitmap != null) {
                                            photo.bitmap.recycle();
                                            photo.bitmap = null;
                                            System.gc();
                                        }
                                    } else {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        if (photo.url.length() > 0) {
                                            Bitmap bitmap = BitmapFactory.decodeFile(
                                                    String.format("%s/%s/photos_cache" +
                                                                    "/wall_photo_attachments/" +
                                                                    "wall_attachment_o%sp%s",
                                                            getContext().getCacheDir(), instance,
                                                            item.repost.newsfeed_item.owner.id,
                                                            item.repost.newsfeed_item.post_id), options);
                                            if (bitmap != null) {
                                                photo.bitmap = bitmap;
                                                attachment.status = "done";
                                                item.repost.newsfeed_item.attachments.set(0, attachment);
                                            }
                                        }
                                    }
                                    wallItems.set(i, item);
                                }
                            }
                        }
                        if (i < firstVisibleItemPosition || i > lastVisibleItemPosition) {
                            if(item.attachments.get(0).type.equals("photo")) {
                                ((Photo) item.attachments.get(0)).bitmap = null;
                            }
                        } else {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            if(item.attachments.size() > 0) {
                                if(item.attachments.get(0).type.equals("photo")) {
                                    Photo photoAttachment = ((Photo) item.attachments.get(0));
                                    if (photoAttachment.url.length() > 0) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(
                                                String.format("%s/%s/photos_cache/wall_photo_attachments/" +
                                                                "wall_attachment_o%sp%s",
                                                        getContext().getCacheDir(), instance,
                                                        item.owner.id, item.post_id), options);
                                        if (bitmap != null) {
                                            ((Photo) item.attachments.get(0)).bitmap = bitmap;
                                            item.attachments.get(0).status = "done";
                                        } else if(photoAttachment.url.length() > 0) {
                                            item.attachments.get(0).status = "error";
                                        }
                                    }
                                } else if(!item.attachments.get(0).type.equals("poll") &&
                                        !item.attachments.get(0).type.equals("video")) {
                                    item.attachments.get(0).status = "not_supported";
                                }
                            }
                        }
                        wallItems.set(i, item);
                    } catch (OutOfMemoryError error) {
                        Log.e("OpenVK Legacy", "Bitmap error: Out of memory");
                    } catch (Exception ex) {
                        if(ex.getMessage() != null) {
                            Log.e("OpenVK Legacy", String.format("Bitmap error: %s",
                                    ex.getMessage()));
                        } else {
                            Log.e("OpenVK Legacy", String.format("Bitmap error: %s",
                                    ex.getClass().getSimpleName()));
                        }
                    }
                }
                wallAdapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setScrollingPositions() {
        if(getChildCount() > 0)
            wallView = (RecyclerView) getChildAt(0);

        if(wallView == null)
            return;

        loadPhotos();
    }

    public void select(int position, String item, int value) {
        if(item.equals("likes")) {
            wallItems.get(position).counters.isLiked = value == 1;
            wallAdapter.notifyDataSetChanged();
        }
    }

    public void select(int position, String item, String value) {
        if(item.equals("likes")) {
            wallItems.get(position).counters.isLiked = value.equals("add");
            wallAdapter.notifyDataSetChanged();
        }
    }

    public void limitWidth(final int width) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int leftWas, int topWas, int rightWas, int bottomWas )
                {
                    int widthWas = rightWas - leftWas;
                    if( v.getWidth() != widthWas ) {
                        if (v.getWidth() > width) {
                            wallView.getLayoutParams().width = width;
                        }
                    }
                }
            });
        }
    }

    public void loadAvatars() {
        if(wallAdapter != null) {
            if(getChildCount() > 0)
                wallView = (RecyclerView) getChildAt(0);

            if(wallView == null)
                return;
            for (int i = 0; i < getCount(); i++) {
                try {
                    WallPost item = wallItems.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/wall_avatars/avatar_%s",
                                    getContext().getCacheDir(), instance, item.author.id), options);
                    if (bitmap != null) {
                        //item.avatar = bitmap;
                    }
                    wallItems.set(i, item);
                } catch (Exception | OutOfMemoryError ex) {
                    ex.printStackTrace();
                }
            }
            wallAdapter.notifyDataSetChanged();
        }
    }

    private int getCount() {
        try {
            return wallView.getAdapter().getItemCount();
        } catch (NullPointerException npE) {
            return 0;
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        adjustLayoutSize(newConfig.orientation);
    }

    public void adjustLayoutSize(int orientation) {
        if(getChildCount() > 0)
            wallView = (RecyclerView) getChildAt(0);

        if(wallView == null)
            return;
        LinearLayout.LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        if (!((OvkApplication) getContext().getApplicationContext()).isTablet) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutParams = new LayoutParams((int)
                        (480 * (getResources().getDisplayMetrics().scaledDensity)),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                limitWidth((int)(480 * (getResources().getDisplayMetrics().scaledDensity)));
            } else {
                layoutParams = new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        }

        layoutParams.gravity = Gravity.CENTER;
        wallView.setLayoutParams(layoutParams);
    }

    public void refreshAdapter() {
        if(wallAdapter != null) {
            wallAdapter.notifyDataSetChanged();
        }
    }

    public NewsfeedAdapter getAdapter() {
        return wallAdapter;
    }

    public void setAudioPlayerState(int status, int position, long post_id) {
        String action = "";
        switch (status) {
            case AudioPlayerService.STATUS_STARTING_FROM_WALL:
                action = "PLAYER_START_FROM_WALL";
                isActivatedAP = true;
                break;
            case AudioPlayerService.STATUS_PLAYING:
                action = "PLAYER_PLAY";
                isActivatedAP = true;
                break;
            case AudioPlayerService.STATUS_PAUSED:
                action = "PLAYER_PAUSE";
                isActivatedAP = true;
                break;
            default:
                action = "PLAYER_STOP";
                isActivatedAP = false;
                break;
        }
        Intent serviceIntent = new Intent(getContext().getApplicationContext(), AudioPlayerService.class);
        serviceIntent.putExtra("action", action);
        if(status == AudioPlayerService.STATUS_STARTING_FROM_WALL) {
            serviceIntent.putExtra("position", position);
            serviceIntent.putExtra("post_id", post_id);
        }
        Log.d(OvkApplication.APP_TAG, "Setting AudioPlayerService state");
        getContext().getApplicationContext().startService(serviceIntent);
    }

    public void closeAudioPlayer() {
        Intent serviceIntent = new Intent(getContext().getApplicationContext(), AudioPlayerService.class);
        serviceIntent.putExtra("action", "PLAYER_STOP");
        Log.d(OvkApplication.APP_TAG, "Setting AudioPlayerService state");
        getContext().getApplicationContext().startService(serviceIntent);
    }

}