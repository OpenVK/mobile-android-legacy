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

package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.Photo;
import uk.openvk.android.legacy.core.activities.PhotoViewerActivity;

public class PhotosListAdapter extends RecyclerView.Adapter<PhotosListAdapter.Holder> {
    private final DisplayImageOptions displayimageOptions;
    private final ImageLoaderConfiguration imageLoaderConfig;
    private final ImageLoader imageLoader;
    private final String instance;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Photo> objects;
    private int photo_fail_count;

    public PhotosListAdapter(Context context, ArrayList<Photo> items) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        instance = PreferenceManager.getDefaultSharedPreferences(ctx).getString("current_instance", "");
        this.displayimageOptions =
                new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.ARGB_8888).build();
        this.imageLoaderConfig =
                new ImageLoaderConfiguration.Builder(ctx.getApplicationContext()).
                        defaultDisplayImageOptions(displayimageOptions)
                        .memoryCacheSize(16777216) // 16 MB memory cache
                        .writeDebugLogs()
                        .build();
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(PhotosListAdapter.this.imageLoaderConfig);
    }

    public Photo getItem(int position) {
        return objects.get(position);
    }

    @Override
    public PhotosListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotosListAdapter.Holder(
                LayoutInflater.from(ctx).inflate(
                        R.layout.list_item_photo_flexible,
                        parent, false
                ));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
    }

    Photo getPhoto(int position) {
        return (getItem(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView item_name;
        public ImageView item_photo;
        public View view;
        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            item_photo = (view.findViewById(R.id.photo_view));
        }

        void bind(final int position) {
            final Photo item = getItem(position);
            loadSmallPicture(item.id, item.album_id, item.owner_id, item_photo);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openPhoto(item);
                }
            });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */
        }

        private void loadSmallPicture(long photo_id, long album_id, long owner_id, ImageView view) {
            try {
                String full_filename = "file://" + ctx.getCacheDir()
                        + "/" + instance + "/photos_cache/album_photos/" +
                        "photo" + photo_id + "_a" + album_id + "_o" + owner_id;
                Bitmap bitmap = imageLoader.loadImageSync(full_filename);
                if(bitmap == null) {
                    view.setImageDrawable(
                            ctx.getResources().getDrawable(R.drawable.photo_loading_black)
                    );
                    return;
                }
                view.setImageBitmap(bitmap);
            } catch (OutOfMemoryError oom) {
                imageLoader.clearMemoryCache();
                imageLoader.clearDiskCache();
                // Retrying again
                if(photo_fail_count < 5) {
                    photo_fail_count++;
                    loadSmallPicture(owner_id, album_id, owner_id, view);
                }
            }
        }

        public void openPhoto(Photo photo) {
            Intent intent = new Intent(ctx.getApplicationContext(), PhotoViewerActivity.class);
            try {
                String full_filename = "file://" + ctx.getCacheDir()
                        + "/" + instance + "/photos_cache/album_photos/" +
                        "photo" + photo.id + "_a" + photo.album_id + "_o" + photo.owner_id;
                intent.putExtra("local_photo_addr", full_filename);
                intent.putExtra("original_link", photo.original_url);
                intent.putExtra("photo_id", photo.id);
                ctx.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}

