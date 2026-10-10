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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.openvk.android.client.base.LazyEntity;
import uk.openvk.android.client.entities.Group;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.attachments.Attachment;
import uk.openvk.android.client.attachments.CommonAttachment;
import uk.openvk.android.client.entities.Audio;
import uk.openvk.android.client.entities.Poll;
import uk.openvk.android.client.entities.Photo;
import uk.openvk.android.client.entities.Video;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.legacy.core.activities.NoteViewerActivity;
import uk.openvk.android.legacy.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.core.activities.VideoPlayerActivity;
import uk.openvk.android.legacy.databases.AudioCacheDB;
import uk.openvk.android.legacy.ui.views.attach.AudioAttachView;
import uk.openvk.android.legacy.ui.views.attach.CommonAttachView;
import uk.openvk.android.legacy.ui.views.attach.PollAttachView;
import uk.openvk.android.legacy.ui.views.attach.VideoAttachView;

import org.apmem.tools.layouts.FlowLayout;

public class MediaAttachmentsView extends LinearLayout {

    private FlowLayout flowLayout;
    private TextView error_label;
    private boolean safeViewing;
    private String instance;
    private SharedPreferences global_prefs;
    private ArrayList<Attachment> attachments;
    private Context parent;
    private int photo_fail_count;
    private ArrayList<Photo> photos;
    private ArrayList<Audio> audios;

    public MediaAttachmentsView(Context ctx) {
        super(ctx);

        parent = ctx;
    }

    public MediaAttachmentsView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        parent = ctx;
    }

    public void prepareAttachments() {
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_post_attachments, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);

        global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        instance = global_prefs.getString("current_instance", "");
        safeViewing = global_prefs.getBoolean("safeViewing", true);
        flowLayout = findViewById(R.id.post_flow_layout);
        error_label = findViewById(R.id.error_label);
    }

    public double getPhotoAspectRatio(Photo photo) {
        return (double)photo.size[0] / (double)photo.size[1];
    }

    public void loadPostAttachments(
            Context ctx, final WallPost post,
            ImageLoader imageLoader, ArrayList<Attachment> attachments,
            int position
    ) {
        if(attachments.size() == 0)
            return;

        if(flowLayout != null)
            flowLayout.removeAllViews();

        if(post.is_explicit && safeViewing)
            return;

        loadAttachments(
                ctx, attachments, imageLoader, post.author,
                "wall_photo_attachments"
        );
    }

    public void loadAttachments(
            Context ctx, ArrayList<Attachment> attachments,
            ImageLoader loader, final LazyEntity author,
            String loadFrom
    ) {

        prepareAttachments();

        this.attachments = attachments;
        this.photos = new ArrayList<>();
        this.audios = new ArrayList<>();

        for (int i = 0; i < attachments.size(); i++) {
            try {
                String type = attachments.get(i).type;
                switch (type) {
                    case "photo":
                        Photo photo = (Photo) attachments.get(i);
                        photo.author = author;
                        photos.add(photo);
                        break;
                    case "video":
                        if (attachments.get(i) != null) {
                            final Video videoAttachment = (Video) attachments.get(i);
                            final VideoAttachView videoView = new VideoAttachView(getContext());
                            videoView.setAttachment(videoAttachment);
                            flowLayout.addView(videoView);
                            videoView.setThumbnail(author.id);
                            videoView.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "poll":
                        if (attachments.get(i) != null) {
                            Poll poll = ((Poll) attachments.get(i));
                            PollAttachView pollView = new PollAttachView(getContext());
                            flowLayout.addView(pollView);
                            pollView.createAdapter(parent, poll);
                            pollView.setPollInfo(poll.question, poll.anonymous,
                                    poll.end_date);
                            pollView.setVisibility(View.VISIBLE);
                        }
                        break;
                    case "note":
                        if (attachments.get(i) != null) {
                            final CommonAttachment commonAttachment =
                                    ((CommonAttachment) attachments.get(i));
                            CommonAttachView commonView = new CommonAttachView(getContext());
                            flowLayout.addView(commonView);
                            commonView.setAttachment(attachments.get(i));
                            commonView.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    viewNoteAttachment(
                                            (CommonAttachView) view,
                                            commonAttachment,
                                            author
                                    );
                                }
                            });
                            commonView.setVisibility(VISIBLE);
                        }
                        break;
                    case "audio":
                        if (attachments.get(i) != null) {
                            Audio audio = ((Audio) attachments.get(i));
                            this.audios.add(audio);
                            AudioAttachView audioView = new AudioAttachView(getContext());
                            flowLayout.addView(audioView);
                            audioView.setAttachment(
                                    ctx, audios.indexOf(audio), author.id, audio
                            );
                            audioView.setVisibility(VISIBLE);
                            int dp = (int) (getResources().getDisplayMetrics().scaledDensity);
                            ((FlowLayout.LayoutParams) audioView.getLayoutParams())
                                    .setMargins(
                                            0,
                                            0,
                                            0,
                                            i < attachments.size() ? 8 * dp : 0
                                    );
                        }
                        break;
                }

                if (!type.equals("note") && !type.equals("audio")) {
                    switch (attachments.get(i).status) {
                        case "not_supported":
                            if (error_label != null) {
                                error_label.setText(
                                        parent.getResources().getString(R.string.not_supported)
                                );
                                error_label.setVisibility(View.VISIBLE);
                            }
                            break;
                        case "error":
                            error_label.setText(
                                    parent.getResources().getString(R.string.attachment_load_err)
                            );
                            error_label.setVisibility(View.VISIBLE);
                            break;
                        default:
                            error_label.setVisibility(View.GONE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if(audios.size() > 0) {
            AudioCacheDB.initDatabase(getContext());
            AudioCacheDB.fillDatabaseFromWall(audios, false);
        }

        if(photos.size() > 1) {
            int max_height = getMaxPhotoHeight(photos);
            int dp = (int) (getResources().getDisplayMetrics().scaledDensity);
            int maxPreviewCount = photos.size() < 6 ? photos.size() : 6;

            for(int i = 0; i < maxPreviewCount; i++) {
                ImageView photoView = new ImageView(getContext());
                Photo photo = photos.get(i);

                photoView.setLayoutParams(
                        maxPreviewCount > 3 ?
                                new FlowLayout.LayoutParams(max_height / 2, max_height / 2) :
                                new FlowLayout.LayoutParams(photo.size[0], max_height)
                );
                ((FlowLayout.LayoutParams) photoView.getLayoutParams())
                        .setMargins(
                                0,
                                0,
                                i < photos.size() - 1 ? 2*dp : 0,
                                4*dp
                        );
                if(((OvkApplication) ctx.getApplicationContext()).isTablet) {
                    FlowLayout.LayoutParams lp = ((FlowLayout.LayoutParams) photoView.getLayoutParams());
                    lp = adjustPhotoView(lp, photos.get(0));
                    photoView.setLayoutParams(lp);
                }
                photoView.setAdjustViewBounds(true);
                photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                loadPhotoPlaceholder(photo, loader, photoView);
                loadPhotoAttachment(i, photoView, loader, loadFrom);
                flowLayout.addView(photoView);
            }
            flowLayout.setVisibility(VISIBLE);

        } else if(photos.size() == 1) {
            ImageView photoView = new ImageView(getContext());
            photoView.setAdjustViewBounds(true);
            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            flowLayout.addView(photoView);

            FlowLayout.LayoutParams lp = ((FlowLayout.LayoutParams) photoView.getLayoutParams());
            lp.weight = 0;
            lp.width = FlowLayout.LayoutParams.MATCH_PARENT;
            if(((OvkApplication) ctx.getApplicationContext()).isTablet) {
                lp = adjustPhotoView(lp, photos.get(0));
            }
            photoView.setLayoutParams(lp);

            loadPhotoPlaceholder( photos.get(0), loader, photoView);
            loadPhotoAttachment(0, photoView, loader, loadFrom);
            photoView.setVisibility(VISIBLE);
        }
        setVisibility(VISIBLE);
    }

    private FlowLayout.LayoutParams adjustPhotoView(FlowLayout.LayoutParams lp, Photo photo) {
        int dp = (int) (getResources().getDisplayMetrics().scaledDensity);
        int res = 0;
        if(getPhotoAspectRatio(photo) >= 1.77) {
            res = 240 * dp;
        } else if(getPhotoAspectRatio(photo) >= 1.32) {
            res = 288 * dp;
        } else if(getPhotoAspectRatio(photo) >= 0.8) {
            res = 320 * dp;
        } else {
            res = 384 * dp;
        }
        lp.width = (int) ((double)res * getPhotoAspectRatio(photo));
        lp.height = res;
        flowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        return lp;
    }

    private int getMinPhotoHeight(ArrayList<Photo> photos) {
        List<Integer> heights = new ArrayList<>();
        for(int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);
            heights.add(photo.size[1]);
        }
        return Collections.min(heights);
    }

    private int getMaxPhotoHeight(ArrayList<Photo> photos) {
        List<Integer> heights = new ArrayList<>();

        boolean isWidescreen =
                ((OvkApplication) getContext().getApplicationContext()).isWidescreen;

        for(int i = 0; i < photos.size(); i++) {
            Photo photo = photos.get(i);

            if(photos.size() <= 3 && photo.size[0] > 0 && photo.size[1] > 0) {
                if (photo.size[0] / photo.size[1] > 1.2) {
                    heights.add(photo.size[1]);
                } else {
                    heights.add(isWidescreen ? 300 : 160);
                }
            } else {
                heights.add(isWidescreen ? 300 : 240);
            }
        }

        int minHeight = Collections.min(heights);
        int maxHeight = Collections.max(heights);
        return (int)(minHeight + ((double)(minHeight + maxHeight) / 2));
    }

    private void loadPhotoPlaceholder(
            final Photo photo, ImageLoader imageLoader, ImageView view
    ) {
        Drawable drawable;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            drawable = parent.getResources().getDrawable(R.drawable.photo_placeholder, getContext().getTheme());
        else
            drawable = parent.getResources().getDrawable(R.drawable.photo_placeholder);

        Canvas canvas = new Canvas();
        try {
            if(photo.size[0] == 0 && photo.size[1] == 0) {
                photo.size[0] = 320;
                photo.size[1] = 320;
            }

            Bitmap bitmap = Bitmap.createBitmap(
                    photo.size[0], photo.size[1], Bitmap.Config.ARGB_8888
            );
            canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, photo.size[0], photo.size[1]);
            drawable.draw(canvas);
            view.setImageBitmap(bitmap);
        } catch (OutOfMemoryError ignored) {
            imageLoader.clearMemoryCache();
            imageLoader.clearDiskCache();
            // Retrying again
            if(photo_fail_count < 5) {
                photo_fail_count++;
                loadPhotoPlaceholder(photo, imageLoader, view);
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPhotoAttachment(photo);
            }
        });
    }

    private void loadPhotoAttachment(
            final int photoPos, final ImageView imageView,
            final ImageLoader imageLoader,
            String loadFrom
    ) {

        final Photo photo = photos.get(photoPos);

        String full_filename =
                String.format("file://%s/%s/photos_cache/%s/%s",
                        parent.getCacheDir(), instance, loadFrom, photo.filename);
        try {
            imageLoader.loadImage(full_filename, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {

                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    photo_fail_count++;
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    if (bitmap != null) {
                        photo.bitmap = bitmap;
                        photos.set(photoPos, photo);

                        imageView.setImageBitmap(bitmap);
                    }
                }

                @Override
                public void onLoadingCancelled(String s, View view) {

                }
            });
        } catch (OutOfMemoryError oom) {
            imageLoader.clearMemoryCache();
            imageLoader.clearDiskCache();
        } catch (Exception e) {
            Log.e(OvkApplication.APP_TAG, String.format("%s: Cannot open file", full_filename));
        }
    }

    private void viewNoteAttachment(
            CommonAttachView attachView,
            CommonAttachment attachment,
            LazyEntity author
    ) {
        Intent intent = new Intent(parent, NoteViewerActivity.class);
        intent.putExtra("id", 0);
        intent.putExtra("title", attachment.title);
        intent.putExtra("content", attachment.text);
        if(author instanceof User) {
            User user = ((User) author);
            intent.putExtra("author",
                    String.format("%s %s", user.first_name, user.last_name)
            );
        } else if(author instanceof Group) {
            Group group = ((Group) author);
            intent.putExtra("author", group.name);
        }
        attachView.setIntent(intent);
        attachView.setVisibility(View.VISIBLE);
    }

    private void playVideo(WallPost item, Video video) {
        Intent intent = new Intent(parent, VideoPlayerActivity.class);
        intent.putExtra("title", video.title);
        intent.putExtra("attachment", (Parcelable) video);
        intent.putExtra("files", video.files);
        intent.putExtra("owner_id", item.owner.id);
        parent.startActivity(intent);
    }

    public void viewPhotoAttachment(Photo photo) {
        Intent intent = new Intent(parent.getApplicationContext(), PhotoViewerActivity.class);

        try {
            if(attachments != null) {
                intent.putExtra("original_link", photo.original_url);
                intent.putExtra("photo_id", photo.id);
                parent.startActivity(intent);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
