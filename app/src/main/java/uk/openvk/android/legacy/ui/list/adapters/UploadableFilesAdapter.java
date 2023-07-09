package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

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
            if(objects.get(i).filename.equals(filename)) {
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

        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            photo_view = convertView.findViewById(R.id.attach_photo_view);
            progress_layout = convertView.findViewById(R.id.upload_layout);
            progress_status = convertView.findViewById(R.id.upload_status);
        }

        void bind(final int position) {
            UploadableFile file = getItem(position);
            if(file.mime.startsWith("image")) {
                loadBitmap(file);
                if(file.progress < file.length) {
                    progress_layout.setVisibility(View.VISIBLE);
                    setUploadProgress(file);
                } else {
                    progress_layout.setVisibility(View.GONE);
                }
            }
        }

        private void loadBitmap(UploadableFile file) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap photo = BitmapFactory.decodeFile(file.filename, options);
            photo_view.setImageBitmap(photo);
        }

        private void setUploadProgress(UploadableFile file) {
            String kb = ctx.getResources().getString(R.string.fsize_kb);
            String mb = ctx.getResources().getString(R.string.fsize_mb);
            String gb = ctx.getResources().getString(R.string.fsize_gb);
            if(file.progress >= 1073741824L) {
                progress_status.setText(
                        String.format("%s / %s %s",
                                file.progress, file.length, gb));
            } else if(file.progress >= 1048576L) {
                progress_status.setText(
                        String.format("%s / %s %s",
                                file.progress, file.length, mb));
            } else if(file.progress >= 1024L) {
                progress_status.setText(
                        String.format("%s / %s %s",
                                file.progress, file.length, kb));
            }
        }
    }
}

