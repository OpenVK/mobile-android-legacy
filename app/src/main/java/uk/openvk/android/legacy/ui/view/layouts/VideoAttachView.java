package uk.openvk.android.legacy.ui.view.layouts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Video;

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

public class VideoAttachView extends FrameLayout {
    private final String instance;
    private Video attachment;
    private Bitmap thumbnail;

    public VideoAttachView(@NonNull Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_video, null);
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        this.addView(view);
    }

    public VideoAttachView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_video, null);
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        this.addView(view);
    }

    @SuppressLint("DefaultLocale")
    public void setAttachment(Video attachment) {
        this.attachment = attachment;
        if(attachment != null) {
            ((TextView) findViewById(R.id.attach_title)).setText(attachment.title);
            if (attachment.duration >= 3600) {
                ((TextView) findViewById(R.id.attach_duration)).setText(
                        String.format("%d:%02d:%02d", attachment.duration / 3600,
                                (attachment.duration % 3600) / 60, (attachment.duration % 60)));
                ((TextView) findViewById(R.id.attach_duration)).setVisibility(VISIBLE);
            } else if(attachment.duration > 0) {
                ((TextView) findViewById(R.id.attach_duration)).setText(
                        String.format("%d:%02d", attachment.duration / 60, (attachment.duration % 60)));
                ((TextView) findViewById(R.id.attach_duration)).setVisibility(VISIBLE);
            } else {
                ((TextView) findViewById(R.id.attach_duration)).setVisibility(GONE);
            }
        }
    }

    public void setThumbnail(long owner_id) {
        ImageView preview = findViewById(R.id.video_preview);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            thumbnail = BitmapFactory.decodeFile(
                    getContext().getCacheDir()
                            + "/" + instance + "/photos_cache/video_thumbnails/thumbnail_"
                            + attachment.id + "o" + owner_id, options);
            preview.setImageBitmap(thumbnail);
        } catch (OutOfMemoryError oom) {
            oom.printStackTrace();
        }
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }
}
