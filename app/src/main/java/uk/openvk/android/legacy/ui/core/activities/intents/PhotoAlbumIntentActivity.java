package uk.openvk.android.legacy.ui.core.activities.intents;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.PhotoAlbum;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.ui.list.adapters.PhotosListAdapter;
import uk.openvk.android.legacy.ui.view.layouts.ProgressLayout;
import uk.openvk.android.legacy.ui.utils.FlexibleGridLayoutManager;

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

public class PhotoAlbumIntentActivity extends NetworkActivity {
    private String access_token;
    private String action;
    private String instance;
    private String args;
    private PhotoAlbum album;
    private RecyclerView photosList;
    public String[] ids;
    private DisplayImageOptions displayimageOptions;
    private ImageLoaderConfiguration imageLoaderConfig;
    private ImageLoader imageLoader;
    private int photo_fail_count;
    private PhotosListAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_album);
        ProgressLayout progressLayout = ((ProgressLayout) findViewById(R.id.progress_layout));
        progressLayout.setVisibility(View.VISIBLE);
        progressLayout.enableDarkTheme(true);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                access_token = instance_prefs.getString("access_token", "");
            } else {
                access_token = instance_prefs.getString("access_token", "");
            }
        } else {
            access_token = (String) savedInstanceState.getSerializable("access_token");
        }

        instance = instance_prefs.getString("server", "");

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = getWindowManager().getDefaultDisplay();
        display.getMetrics(metrics);

        final Uri uri = getIntent().getData();

        if (uri != null) {
            String path = uri.toString();
            if (instance_prefs.getString("access_token", "").length() == 0) {
                finish();
                return;
            }
            try {
                args = Global.getUrlArguments(path);
                album = new PhotoAlbum(args);
                if(args.length() > 0) {
                    installLayouts();
                }
                ids = args.split("_");
                ovk_api.photos.getAlbums(ovk_api.wrapper, Long.parseLong(ids[1]), 100,
                        true, false, false);
            } catch (Exception ex) {
                ex.printStackTrace();
                finish();
            }
        }
        initalizeImageLoader();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            try {
                try {
                    getActionBar().setDisplayShowHomeEnabled(true);
                    getActionBar().setDisplayHomeAsUpEnabled(true);
                    getActionBar().setTitle(getResources().getString(R.string.photo));
                    getActionBar().setBackgroundDrawable(
                          getResources().getDrawable(R.drawable.transparent_black_gradient_top_v2));
                    getActionBar().setDisplayShowTitleEnabled(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    getActionBar().setIcon(R.drawable.ic_ab_app);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        setTranslucentStatusBar(1, android.R.color.black);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (item.getItemId() == android.R.id.home) {
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void initalizeImageLoader() {
        this.displayimageOptions =
                new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.ARGB_8888).build();
        this.imageLoaderConfig =
                new ImageLoaderConfiguration.Builder(getApplicationContext()).
                        defaultDisplayImageOptions(displayimageOptions)
                        .memoryCacheSize(16777216) // 16 MB memory cache
                        .writeDebugLogs()
                        .build();
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(this.imageLoaderConfig);
    }

    private void installLayouts() {
        
    }

    @Override
    public void receiveState(int message, Bundle data) {
        try {
            if(data.containsKey("address")) {
                String activityName = data.getString("address");
                if(activityName == null) {
                    return;
                }
                boolean isCurrentActivity = activityName.equals(getLocalClassName());
                if(!isCurrentActivity) {
                    return;
                }
            }
            if(message == HandlerMessages.PHOTOS_GETALBUMS) {
                ovk_api.photos.getByAlbumId(
                        ovk_api.wrapper,
                        Long.parseLong(ids[0]),
                        Long.parseLong(ids[1]),
                        75,
                        true);
            } else if(message == HandlerMessages.PHOTOS_GET) {
                createPhotoAlbumAdapter();
                for(int i = 0; i < ovk_api.photos.albumsList.size(); i++) {
                    if(ovk_api.photos.albumsList.get(i).ids[0] == Long.parseLong(ids[1])) {
                        ovk_api.photos.album.title = ovk_api.photos.albumsList.get(i).title;
                    }
                }
                ((TextView) findViewById(R.id.album_title)).setText(ovk_api.photos.album.title);
                ((TextView) findViewById(R.id.album_count)).setText(
                        Global.getPluralQuantityString(this, R.plurals.photos,
                                Integer.parseInt(String.valueOf(ovk_api.photos.album.size)))
                );
                loadAlbumThumbnail(
                        Long.parseLong(ids[0]),
                        Long.parseLong(ids[1]),
                        (ImageView) findViewById(R.id.album_thumb));
            } else if(message == HandlerMessages.ALBUM_PHOTOS) {
                adapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
}

    private void createPhotoAlbumAdapter() {
        adapter = new PhotosListAdapter(this, ovk_api.photos.album.photos);
        photosList = findViewById(R.id.photos_listview);
        FlexibleGridLayoutManager flex_lm =
                new FlexibleGridLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        flex_lm.setColumnCountProvider(new FlexibleGridLayoutManager.ColumnCountProvider() {
            @Override
            public int getColumnCount(int recyclerViewWidth) {
                return recyclerViewWidth / (int)(62 * getResources().getDisplayMetrics().scaledDensity);
            }
        });
        photosList.setLayoutManager(flex_lm);
        photosList.setAdapter(adapter);
        findViewById(R.id.progress_layout).setVisibility(View.GONE);
    }

    private void loadAlbumThumbnail(long owner_id, long album_id, ImageView view) {
        try {
            String full_filename = "file://" + getCacheDir()
                    + "/" + instance + "/photos_cache/photo_albums/" +
                    "photo_album_" + owner_id + "_" + album_id;
            Bitmap bitmap = imageLoader.loadImageSync(full_filename);
            view.setImageBitmap(bitmap);
        } catch (OutOfMemoryError oom) {
            imageLoader.clearMemoryCache();
            imageLoader.clearDiskCache();
            // Retrying again
            if(photo_fail_count < 5) {
                photo_fail_count++;
                loadAlbumThumbnail(owner_id, album_id, view);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            findViewById(R.id.album_header).getLayoutParams().height =
                    (int)(180 * getResources().getDisplayMetrics().scaledDensity);
        } else {
            findViewById(R.id.album_header).getLayoutParams().height =
                    (int)(240 * getResources().getDisplayMetrics().scaledDensity);
        }
    }
}
