package uk.openvk.android.legacy.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.ui.core.activities.base.UsersListActivity;
import uk.openvk.android.legacy.ui.core.fragments.base.UsersFragment;
import uk.openvk.android.legacy.ui.list.items.UploadableFile;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class UploadableFilesAdapter extends RecyclerView.Adapter<UploadableFilesAdapter.Holder> {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<UploadableFile> objects;
    public boolean opened_sliding_menu;

    public UploadableFilesAdapter(Context context, ArrayList<UploadableFile> items) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public UploadableFile getItem(int position) {
        return objects.get(position);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                LayoutInflater.from(ctx).inflate(R.layout.attach_upload, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int searchByFileName(String filename) {
        int result = 0;
        for(int i = 0; i < objects.size(); i++) {
            if(objects.get(i).filename.endsWith(filename)) {
                result = i;
            }
        }
        return result;
    }

    public class Holder extends RecyclerView.ViewHolder {
        private View view;
        public ImageView photo_view;
        public TextView item_title;
        public TextView item_subtitle;
        public LinearLayout progress_layout;
        public TextView progress_status;
        public ProgressBar progress;
        public ImageView error_icon;

        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            photo_view = convertView.findViewById(R.id.attach_photo_view);
            progress_layout = convertView.findViewById(R.id.upload_layout);
            progress_status = convertView.findViewById(R.id.upload_status);
            progress = convertView.findViewById(R.id.upload_progress);
            error_icon = convertView.findViewById(R.id.error_icon);
        }

        void bind(final int position) {
            UploadableFile file = getItem(position);
            if(file.mime.startsWith("image")) {
                loadBitmap(file);
                setUploadProgress(file);
                if(file.progress < file.length) {
                    progress_layout.setVisibility(View.VISIBLE);
                } else {
                    if(file.status.equals("uploaded")) {
                        progress_layout.setVisibility(View.GONE);
                    }
                }

                if(file.status.equals("error")) {
                    progress_layout.setVisibility(View.VISIBLE);
                    progress.setVisibility(View.GONE);
                    error_icon.setVisibility(View.VISIBLE);
                    progress_status.setText(ctx.getResources().getString(R.string.error));
                }
            }
        }

        private void loadBitmap(UploadableFile file) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap photo = BitmapFactory.decodeFile(file.filename, options);
                if(photo != null) {
                    if (photo.getWidth() > 600 && photo.getHeight() > 600) {
                        Bitmap photo_scaled = Bitmap.createScaledBitmap(photo, 600,
                                (int) ((double) 600 / ((double) photo.getWidth() / (double) photo.getHeight())),
                                false);
                        photo.recycle();
                        photo = null;
                        photo_view.setImageBitmap(photo_scaled);
                    } else {
                        photo_view.setImageBitmap(photo);
                    }
                } else {
                    photo_view.setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
                }
            } catch (OutOfMemoryError ignored) {

            }
        }

        @SuppressWarnings("MalformedFormatString")
        @SuppressLint("DefaultLocale")
        private void setUploadProgress(UploadableFile file) {
            String b = ctx.getResources().getString(R.string.fsize_b);
            String kb = ctx.getResources().getString(R.string.fsize_kb);
            String mb = ctx.getResources().getString(R.string.fsize_mb);
            String gb = ctx.getResources().getString(R.string.fsize_gb);
            if(file.length >= 1073741824L) {
                progress_status.setText(
                        String.format("%.1f / %.1f %s",
                                (double)file.progress / (double)1024 / (double)1024 / (double)1024,
                                (double)file.length / (double)1024 / (double)1024 / (double)1024, gb));
            } else if(file.length >= 104857600L) {
                progress_status.setText(
                        String.format("%d / %d %s",
                                (double)file.progress / (double)1024 / (double)1024,
                                (double)file.length / (double)1024 / (double)1024, mb));
            } else if(file.length >= 1048576L) {
                progress_status.setText(
                        String.format("%.1f / %.1f %s",
                                (double)file.progress / (double)1024 / (double)1024,
                                (double)file.length / (double)1024 / (double)1024, mb));
            } else if(file.length >= 102400L) {
                progress_status.setText(
                        String.format("%d / %d %s",
                                (double)file.progress / (double)1024, (double)file.length / (double)1024, kb));
            } else if(file.length >= 1024L) {
                progress_status.setText(
                        String.format("%.1f / %.1f %s",
                                (double)file.progress / (double)1024, (double)file.length / (double)1024, kb));
            } else {
                progress_status.setText(
                        String.format("%s / %s %s",
                                file.progress, file.length, b));
            }
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }
}

