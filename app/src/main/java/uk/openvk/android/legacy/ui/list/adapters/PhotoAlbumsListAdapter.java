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
import android.net.Uri;
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

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.PhotoAlbum;

public class PhotoAlbumsListAdapter extends RecyclerView.Adapter<PhotoAlbumsListAdapter.Holder> {
    private final DisplayImageOptions displayimageOptions;
    private final ImageLoaderConfiguration imageLoaderConfig;
    private final ImageLoader imageLoader;
    private final String instance;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<PhotoAlbum> objects;
    private int photo_fail_count;

    public PhotoAlbumsListAdapter(Context context, ArrayList<PhotoAlbum> items) {
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
        imageLoader.init(PhotoAlbumsListAdapter.this.imageLoaderConfig);
    }

    public PhotoAlbum getItem(int position) {
        return objects.get(position);
    }

    @Override
    public PhotoAlbumsListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PhotoAlbumsListAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.attach_album_2,
                parent, false));
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

    PhotoAlbum getAlbum(int position) {
        return (getItem(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_count;
        public ImageView item_thumbnail;
        public View view;
        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            item_name = (view.findViewById(R.id.attach_title));
            item_count = (view.findViewById(R.id.attach_count));
            item_thumbnail = (view.findViewById(R.id.album_preview));
        }

        void bind(final int position) {
            final PhotoAlbum item = getItem(position);
            item_name.setText(item.title);
            item_count.setText(
                    String.format("%s",
                            Global.getPluralQuantityString(
                                    ctx, R.plurals.photos,
                                    Integer.parseInt(String.valueOf(item.size))
                            )
                    )
            );
            loadAlbumThumbnail(item.ids[0], item.ids[1], item_thumbnail);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openAlbum(item);
                }
            });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */
        }

        private void loadAlbumThumbnail(long owner_id, long album_id, ImageView view) {
            try {
                String full_filename = "file://" + ctx.getCacheDir()
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

        public void openAlbum(PhotoAlbum album) {
            String url = "openvk://ovk/album" + album.ids[0] + "_" + album.ids[1];
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.setPackage("uk.openvk.android.legacy");
            ctx.startActivity(i);
        }
    }

}

