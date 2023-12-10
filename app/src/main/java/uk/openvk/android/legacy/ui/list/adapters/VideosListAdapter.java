package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Video;
import uk.openvk.android.legacy.core.activities.VideoPlayerActivity;

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

public class VideosListAdapter extends RecyclerView.Adapter<VideosListAdapter.Holder> {
    private final DisplayImageOptions displayimageOptions;
    private final ImageLoaderConfiguration imageLoaderConfig;
    private final ImageLoader imageLoader;
    private final String instance;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Video> objects;
    private int photo_fail_count;

    public VideosListAdapter(Context context, ArrayList<Video> items) {
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
        imageLoader.init(VideosListAdapter.this.imageLoaderConfig);
    }

    public Video getItem(int position) {
        return objects.get(position);
    }

    @Override
    public VideosListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideosListAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_video,
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

    Video getVideo(int position) {
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
            item_name = (view.findViewById(R.id.video_title));
            item_count = (view.findViewById(R.id.video_qty));
            item_thumbnail = (view.findViewById(R.id.video_thumbnail));
        }

        void bind(final int position) {
            final Video item = getItem(position);
            item_name.setText(item.title);
            item_count.setVisibility(View.GONE);
            loadVideoThumbnail(item.owner_id, item.id, item_thumbnail);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openVideo(item);
                }
            });
            Log.d(OvkApplication.APP_TAG,
                    String.format("Video #%s / Item ID: %s / Owner ID: %s", position, item.id, item.owner_id));

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */
        }

        private void loadVideoThumbnail(long owner_id, long video_id, ImageView view) {
            try {
                String full_filename = "file://" + ctx.getCacheDir()
                        + "/" + instance + "/photos_cache/video_thumbnails/" +
                        "thumbnail_" + video_id + "o" + owner_id;
                Bitmap bitmap = imageLoader.loadImageSync(full_filename);
                view.setImageBitmap(bitmap);
            } catch (OutOfMemoryError oom) {
                imageLoader.clearMemoryCache();
                imageLoader.clearDiskCache();
                // Retrying again
                if(photo_fail_count < 5) {
                    photo_fail_count++;
                    loadVideoThumbnail(owner_id, video_id, view);
                }
            } catch (Exception ignored) {

            }
        }

        public void openVideo(Video video) {
            Intent intent = new Intent(ctx, VideoPlayerActivity.class);
            intent.putExtra("title", video.title);
            intent.putExtra("attachment", (Parcelable) video);
            intent.putExtra("files", video.files);
            intent.putExtra("owner_id", video.owner_id);
            ctx.startActivity(intent);
        }
    }

}

