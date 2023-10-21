package uk.openvk.android.legacy.ui.list.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import dev.tinelix.retro_pm.MenuItem;
import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.attachments.CommonAttachment;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;
import uk.openvk.android.legacy.api.entities.OvkExpandableText;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.WallPostActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.NoteActivity;
import uk.openvk.android.legacy.ui.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.VideoPlayerActivity;
import uk.openvk.android.legacy.ui.core.fragments.app.NewsfeedFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.legacy.ui.view.layouts.CommonAttachView;
import uk.openvk.android.legacy.ui.view.layouts.PollAttachView;
import uk.openvk.android.legacy.ui.view.layouts.VideoAttachView;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;

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

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.Holder> {

    private final String instance;
    private final boolean isWall;
    private final ImageLoaderConfiguration imageLoaderConfig;
    private final DisplayImageOptions displayimageOptions;
    private final ImageLoader imageLoader;
    private boolean safeViewing;
    private String where;
    private ArrayList<WallPost> items = new ArrayList<>();
    private Context ctx;
    public LruCache memCache;
    private int resize_videoattachviews;
    private int resize_photoattachments;
    private int photo_fail_count;

    public NewsfeedAdapter(Context context, ArrayList<WallPost> posts, boolean isWall) {
        ctx = context;
        items = posts;
        instance = PreferenceManager.getDefaultSharedPreferences(ctx).getString("current_instance", "");
        safeViewing = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("safeViewing", true);
        this.isWall = isWall;
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
        imageLoader.init(NewsfeedAdapter.this.imageLoaderConfig);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_newsfeed, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
        holder.post_photo.setImageBitmap(null);
        holder.post_photo.setVisibility(View.GONE);
    }

    public WallPost getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final TextView poster_name;
        public final TextView post_info;
        public final TextView post_text;
        public final ImageView post_photo;
        public final LinearLayout repost_info;
        public final TextView original_poster_name;
        public final TextView original_post_info;
        public final TextView original_post_text;
        public final TextView likes_counter;
        public final TextView reposts_counter;
        public final TextView comments_counter;
        public final View convertView;
        public final ImageView avatar;
        private final ProgressBar photo_progress;
        private final TextView error_label;
        private final PollAttachView pollAttachView;
        private final ImageView original_post_photo;
        private final PollAttachView original_post_poll;
        private final TextView expand_text_btn;
        private final TextView repost_expand_text_btn;
        private final ImageView api_app_indicator;
        private final VideoAttachView post_video;
        private final ImageView verified_icon;
        private final CommonAttachView attachment_view;
        private final ImageButton options_btn;
        private PopupMenu p_menu;
        private boolean likeAdded = false;
        private boolean likeDeleted = false;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.poster_name = view.findViewById(R.id.poster_name_view);
            this.post_info = view.findViewById(R.id.post_info_view);
            this.post_text = view.findViewById(R.id.post_view);
            this.post_photo = view.findViewById(R.id.post_photo);
            this.post_video = view.findViewById(R.id.post_video);
            this.likes_counter = view.findViewById(R.id.post_likes);
            this.reposts_counter = view.findViewById(R.id.post_reposts);
            this.comments_counter = view.findViewById(R.id.post_comments);
            this.avatar = view.findViewById(R.id.author_avatar);
            this.photo_progress = (view.findViewById(R.id.photo_progress));
            this.error_label = (convertView.findViewById(R.id.error_label));
            this.pollAttachView = (convertView.findViewById(R.id.poll_layout));
            this.attachment_view = (convertView.findViewById(R.id.post_attahcment));
            this.repost_info = (convertView.findViewById(R.id.post_attach_container));
            this.original_poster_name = (convertView.findViewById(R.id.post_retweet_name));
            this.original_post_info = (convertView.findViewById(R.id.post_retweet_time));
            this.original_post_text = (convertView.findViewById(R.id.post_retweet_text));
            this.original_post_photo = view.findViewById(R.id.repost_photo);
            this.original_post_poll = view.findViewById(R.id.repost_poll_layout);
            this.expand_text_btn = view.findViewById(R.id.expand_text_btn);
            this.repost_expand_text_btn = view.findViewById(R.id.repost_expand_text_btn);
            this.api_app_indicator = view.findViewById(R.id.api_app_indicator);
            this.verified_icon = view.findViewById(R.id.verified_icon);
            this.options_btn = view.findViewById(R.id.post_options_btn);
        }

        void bind(final int position) {
            final WallPost item = getItem(position);

            options_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPostOptions(view);
                }
            });

            if(item.post_source.type.equals("api")) {
                api_app_indicator.setVisibility(View.VISIBLE);
                switch (item.post_source.platform) {
                    case "android":
                        api_app_indicator.setImageDrawable(ctx.getResources().
                                getDrawable(R.drawable.ic_api_android_app_indicator));
                        break;
                    case "iphone":
                        api_app_indicator.setImageDrawable(ctx.getResources().getDrawable(
                                R.drawable.ic_api_ios_app_indicator));
                        break;
                    case "mobile":
                        api_app_indicator.setImageDrawable(ctx.getResources().getDrawable(
                                R.drawable.ic_api_mobile_indicator));
                        break;
                    default:
                        api_app_indicator.setVisibility(View.GONE);
                        break;
                }
            } else {
                api_app_indicator.setVisibility(View.GONE);
            }
            poster_name.setText(item.name);
            if(item.verified_author) {
                verified_icon.setVisibility(View.VISIBLE);
            } else {
                verified_icon.setVisibility(View.GONE);
            }
            post_info.setText(item.info);
            expand_text_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWallComments(ctx, position, null);
                }
            });
            if(!item.is_explicit || !safeViewing) {
                if (item.text.length() > 0) {
                    post_text.setVisibility(View.VISIBLE);
                    String text = item.text.replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&amp;", "&")
                            .replaceAll("&quot;", "\"");
                    String[] lines = text.split("\r\n|\r|\n");
                    String text_llines = "";
                    if (lines.length > 8) {
                        for (int line_no = 0; line_no < 8; line_no++) {
                            if (line_no == 7) {
                                if (lines[line_no].length() > 0)
                                    text_llines += String.format("%s...", lines[line_no]);
                            } else if (line_no == 6) {
                                if (lines[line_no + 1].length() == 0) {
                                    text_llines += String.format("%s", lines[line_no]);
                                } else {
                                    text_llines += String.format("%s\r\n", lines[line_no]);
                                }
                            } else {
                                text_llines += String.format("%s\r\n", lines[line_no]);
                            }
                        }
                        post_text.setText(Global.formatLinksAsHtml(text_llines));
                        expand_text_btn.setVisibility(View.VISIBLE);
                    } else {
                        OvkExpandableText expandableText = Global.formatLinksAsHtml(text, 500);
                        post_text.setText(expandableText.sp_text);
                        if (expandableText.expandable) {
                            expand_text_btn.setVisibility(View.VISIBLE);
                        } else {
                            expand_text_btn.setVisibility(View.GONE);
                        }
                    }
                } else {
                    post_text.setVisibility(View.GONE);
                    expand_text_btn.setVisibility(View.GONE);
                }

                if (item.repost != null) {
                    repost_info.setVisibility(View.VISIBLE);
                    original_poster_name.setText(item.repost.name);
                    original_post_info.setText(item.repost.time);
                    String repost_text = item.repost.newsfeed_item.text.replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
                    String[] repost_lines = item.repost.newsfeed_item.text.split("\r\n|\r|\n");
                    if (repost_lines.length > 8 && item.repost.newsfeed_item.text.length() <= 500) {
                        String text_llines = "";
                        for (int line_no = 0; line_no < 8; line_no++) {
                            if (line_no == 7) {
                                text_llines += String.format("%s...", repost_lines[line_no]);
                            } else {
                                text_llines += String.format("%s\r\n", repost_lines[line_no]);
                            }
                        }
                        original_post_text.setText(text_llines);
                        repost_expand_text_btn.setVisibility(View.VISIBLE);
                    } else if (repost_text.length() > 500) {
                        original_post_text.setText(String.format("%s...", repost_text.substring(0, 500)));
                        repost_expand_text_btn.setVisibility(View.VISIBLE);
                    } else {
                        original_post_text.setText(repost_text);
                        repost_expand_text_btn.setVisibility(View.GONE);
                    }
                    for (int i = 0; i < item.repost.newsfeed_item.attachments.size(); i++) {
                        if (item.repost.newsfeed_item.attachments.get(i).status.equals("loading")) {
                            try {
                                photo_progress.setVisibility(View.VISIBLE);
                            } catch (Exception ignored) {
                            }
                            original_post_photo.setImageBitmap(null);
                        } else if (item.repost.newsfeed_item.attachments.get(i).status.equals("not_supported")) {
                            error_label.setText(ctx.getResources().getString(R.string.not_supported));
                            error_label.setVisibility(View.VISIBLE);
                        } else if (item.repost.newsfeed_item.attachments.get(i).type.equals("photo")) {
                            if (item.repost.newsfeed_item.attachments.get(i).getContent() != null) {
                                WallPost repost = item.repost.newsfeed_item;
                                if (repost.attachments.get(i).status.equals("done")) {
                                    loadPhotoAttachment((PhotoAttachment) repost.attachments.get(i).getContent(),
                                            repost.owner_id, repost.post_id, original_post_photo);
                                } else {
                                    loadPhotoPlaceholder(repost, (PhotoAttachment) repost.attachments.get(i).getContent(),
                                            original_post_photo);
                                }
                                original_post_photo.setVisibility(View.VISIBLE);
                            }
                        } else if (item.repost.newsfeed_item.attachments.get(i).type.equals("poll")) {
                            if (item.repost.newsfeed_item.attachments.get(i).getContent() != null) {
                                PollAttachment pollAttachment = ((PollAttachment) item.repost.
                                        newsfeed_item.attachments.get(i).getContent());
                                original_post_poll.createAdapter(ctx, position, pollAttachment.answers,
                                        pollAttachment.multiple, pollAttachment.user_votes, pollAttachment.votes);
                                original_post_poll.setPollInfo(pollAttachment.question,
                                        pollAttachment.anonymous, pollAttachment.end_date);
                                original_post_poll.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    repost_info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openWallRepostComments(ctx, position, view);
                        }
                    });
                } else {
                    repost_info.setVisibility(View.GONE);
                }
            } else {
                error_label.setText(ctx.getResources().getString(R.string.post_load_nsfw));
                error_label.setVisibility(View.VISIBLE);
                post_text.setVisibility(View.GONE);
            }

            if(!item.is_explicit || !safeViewing) {
                error_label.setVisibility(View.GONE);
            }
            post_photo.setVisibility(View.GONE);
            post_video.setVisibility(View.GONE);
            pollAttachView.setVisibility(View.GONE);

            if(!item.is_explicit || !safeViewing) {
                for (int i = 0; i < item.attachments.size(); i++) {
                    if (item.attachments.get(i).type.equals("photo")) {
                        post_photo.setVisibility(View.VISIBLE);
                        PhotoAttachment photoAttachment = (PhotoAttachment) item.attachments.get(i).getContent();
                        if (item.attachments.get(i).status.equals("done")) {
                            loadPhotoAttachment(photoAttachment, item.owner_id, item.post_id, post_photo);
                        } else {
                            loadPhotoPlaceholder(item, photoAttachment, post_photo);
                        }
                    } else if (item.attachments.get(i).status.equals("not_supported") &&
                            !item.attachments.get(i).type.equals("note")) {
                        error_label.setText(ctx.getResources().getString(R.string.not_supported));
                        error_label.setVisibility(View.VISIBLE);
                    } else if (item.attachments.get(i).status.equals("error")) {
                        error_label.setText(ctx.getResources().getString(R.string.attachment_load_err));
                        error_label.setVisibility(View.VISIBLE);
                    } else if (item.attachments.get(i).status.equals("done") &&
                            item.attachments.get(i).type.equals("video")) {
                        if (item.attachments.get(i).getContent() != null) {
                            final VideoAttachment videoAttachment = (VideoAttachment)
                                    item.attachments.get(i).getContent();
                            post_video.setAttachment(videoAttachment);
                            post_video.setVisibility(View.VISIBLE);
                            post_video.setThumbnail(item.owner_id);
                            if (resize_videoattachviews < 1) {
                                post_video.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        float widescreen_aspect_ratio = post_video.getMeasuredWidth() / 16;
                                        float attachment_height = widescreen_aspect_ratio * 9;
                                        LinearLayout.LayoutParams lp =
                                                ((LinearLayout.LayoutParams) post_video.getLayoutParams());
                                        lp.height = (int) attachment_height;
                                        post_video.setLayoutParams(lp);
                                    }
                                });
                                resize_videoattachviews++;
                            }
                            post_video.getViewTreeObserver().addOnGlobalLayoutListener(
                                    new ViewTreeObserver.OnGlobalLayoutListener() {
                                        @Override
                                        public void onGlobalLayout() {
                                            float widescreen_aspect_ratio = post_video.getMeasuredWidth() / 16;
                                            float attachment_height = widescreen_aspect_ratio * 9;
                                            post_video.getLayoutParams().height = (int) attachment_height;
                                        }
                                    });
                            post_video.findViewById(R.id.video_att_view).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(ctx, VideoPlayerActivity.class);
                                    intent.putExtra("title", videoAttachment.title);
                                    intent.putExtra("attachment", videoAttachment);
                                    intent.putExtra("files", videoAttachment.files);
                                    intent.putExtra("owner_id", item.owner_id);
                                    ctx.startActivity(intent);
                                }
                            });
                        }
                    } else if (item.attachments.get(i).type.equals("poll")) {
                        if (item.attachments.get(i).getContent() != null) {
                            PollAttachment pollAttachment = ((PollAttachment)
                                    item.attachments.get(i).getContent());
                            pollAttachView.createAdapter(ctx, position, pollAttachment.answers,
                                    pollAttachment.multiple, pollAttachment.user_votes, pollAttachment.votes);
                            pollAttachView.setPollInfo(pollAttachment.question, pollAttachment.anonymous,
                                    pollAttachment.end_date);
                            pollAttachView.setVisibility(View.VISIBLE);
                        }
                    } else if (item.attachments.get(i).type.equals("note")) {
                        if (item.attachments.get(i).getContent() != null) {
                            CommonAttachment commonAttachment = ((CommonAttachment)
                                    item.attachments.get(i).getContent());
                            attachment_view.setAttachment(item.attachments.get(i));
                            Intent intent = new Intent(ctx, NoteActivity.class);
                            intent.putExtra("title", commonAttachment.title);
                            intent.putExtra("content", commonAttachment.text);
                            intent.putExtra("author", item.name);
                            attachment_view.setIntent(intent);
                            attachment_view.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }

            likes_counter.setText(String.format("%s", item.counters.likes));
            reposts_counter.setText(String.format("%s", item.counters.reposts));
            comments_counter.setText(String.format("%s", item.counters.comments));

            if(item.counters.isLiked) {
                likes_counter.setSelected(true);
            } else {
                likes_counter.setSelected(false);
            }

            if(item.counters.enabled) {
                likes_counter.setEnabled(true);
                if(item.counters.isLiked && likeAdded) {
                    likes_counter.setText(String.format("%s", item.counters.likes + 1));
                } else if(!item.counters.isLiked && likeDeleted) {
                    likes_counter.setText(String.format("%s", item.counters.likes - 1));
                } else {
                    likes_counter.setText(String.format("%s", item.counters.likes));
                }
            } else {
                likes_counter.setEnabled(false);
            }

            Bitmap author_avatar = item.avatar;
            if(author_avatar != null) {
                avatar.setImageBitmap(author_avatar);
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/newsfeed_avatars/avatar_%s",
                                    ctx.getCacheDir(), instance, item.author_id), options);
                    if (bitmap != null) {
                        avatar.setImageBitmap(bitmap);
                    } else {
                        avatar.setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
                    }
                } catch (OutOfMemoryError ignored) {

                }
            }

            ((LinearLayout) convertView.findViewById(R.id.poster_ll)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx instanceof AppActivity) {
                        String where = "";
                        if(((AppActivity) ctx).selectedFragment instanceof NewsfeedFragment) {
                            where = "newsfeed";
                        } else {
                            where = "profile";
                        }
                        showAuthorPage(ctx, where, position);
                    } else {
                        showAuthorPage(ctx, "profile", position);
                    }
                }
            });

            likes_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.counters.isLiked) {
                        if(!likeAdded) {
                            likeDeleted = true;
                        }
                        deleteLike(ctx, position, "post", view);
                    } else {
                        if(!likeDeleted) {
                            likeAdded = true;
                        }
                        addLike(ctx, position, "post", view);
                    }
                }
            });

            reposts_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    repost(position);
                }
            });

            comments_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openWallComments(ctx, position, view);
                }
            });
        }

        private void showPostOptions(View view) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                android.widget.PopupMenu popupMenu =
                        new android.widget.PopupMenu(view.getContext(), view);
                popupMenu.getMenu().add(ctx.getResources().getString(R.string.report_content));
                popupMenu.show();
            } else {
                p_menu = new PopupMenu(ctx);
                p_menu.setHeaderTitle("");
                p_menu.add(0, ctx.getResources().getString(R.string.report_content));
                p_menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(MenuItem item) {
                        Toast.makeText
                                (ctx,
                                        ctx.getResources().getString(R.string.not_implemented),
                                        Toast.LENGTH_LONG).show();
                    }
                });
                p_menu.show(view);
            }
        }

        private void loadPhotoPlaceholder(final WallPost post, PhotoAttachment photoAttachment, ImageView view) {
            Drawable drawable = ctx.getResources().getDrawable(R.drawable.photo_placeholder);
            Canvas canvas = new Canvas();
            try {
                Bitmap bitmap = Bitmap.createBitmap(
                        photoAttachment.size[0], photoAttachment.size[1], Bitmap.Config.ARGB_8888
                );
                canvas.setBitmap(bitmap);
                drawable.setBounds(0, 0, photoAttachment.size[0], photoAttachment.size[1]);
                drawable.draw(canvas);
                view.setImageBitmap(bitmap);
            } catch (OutOfMemoryError ignored) {
                imageLoader.clearMemoryCache();
                imageLoader.clearDiskCache();
                // Retrying again
                if(photo_fail_count < 5) {
                    photo_fail_count++;
                    loadPhotoPlaceholder(post, photoAttachment, view);
                }
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewPhotoAttachment(post);
                }
            });
        }

        private void loadPhotoAttachment(PhotoAttachment photoAttachment,
                                         long owner_id, long post_id, ImageView view) {
            try {
                String full_filename = "file://" + ctx.getCacheDir()
                        + "/" + instance + "/photos_cache/newsfeed_photo_attachments/" +
                        photoAttachment.filename;
                if (isWall) {
                    full_filename = "file://" + ctx.getCacheDir()
                            + "/" + instance + "/photos_cache/wall_photo_attachments/" +
                            photoAttachment.filename;
                }

                Bitmap bitmap = imageLoader.loadImageSync(full_filename);
                view.setImageBitmap(bitmap);
            } catch (OutOfMemoryError oom) {
                imageLoader.clearMemoryCache();
                imageLoader.clearDiskCache();
                // Retrying again
                if(photo_fail_count < 5) {
                    photo_fail_count++;
                    loadPhotoAttachment(photoAttachment, owner_id, post_id, view);
                }
            }
        }

        public void repost(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            final ArrayList<String> functions = new ArrayList<>();
            builder.setTitle(R.string.repost_dlg_title);
            functions.add(ctx.getResources().getString(R.string.repost_own_wall));
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(ctx, R.layout.list_item_select_dialog, R.id.text, functions);
            builder.setSingleChoiceItems(adapter, -1, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            final WallPost finalPost = getItem(position);
            SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String current_screen = global_prefs.getString("current_screen", "");
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(functions.get(position)
                            .equals(ctx.getResources().getString(R.string.repost_own_wall))) {
                        if(ctx instanceof AppActivity) {
                            Global.openRepostDialog(ctx,
                                    ((AppActivity) ctx).ovk_api,
                                    "own_wall", finalPost);
                        } else if(ctx instanceof ProfileIntentActivity) {
                            Global.openRepostDialog(ctx,
                                    ((ProfileIntentActivity) ctx).ovk_api,
                                    "own_wall", finalPost);
                        } else if(ctx instanceof GroupIntentActivity) {
                            Global.openRepostDialog(ctx,
                                    ((GroupIntentActivity) ctx).ovk_api,
                                    "own_wall", finalPost);
                        }
                        dialog.dismiss();
                    }
                }
            });
        }

        public void viewPhotoAttachment(WallPost post) {
            WallPost item;
            Intent intent = new Intent(ctx.getApplicationContext(), PhotoViewerActivity.class);
            if (isWall) {
                intent.putExtra("where", "wall");
            } else {
                intent.putExtra("where", "newsfeed");
            }
            try {
                if (isWall) {
                    intent.putExtra("local_photo_addr",
                            String.format("%s/wall_photo_attachments/wall_attachment_o%sp%s",
                                    ctx.getCacheDir(),
                                    post.owner_id, post.post_id));
                } else {
                    intent.putExtra("local_photo_addr",
                            String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s",
                                    ctx.getCacheDir(),
                                    post.owner_id, post.post_id));
                }
                if(post.attachments != null) {
                    for(int i = 0; i < post.attachments.size(); i++) {
                        if(post.attachments.get(i).type.equals("photo")) {
                            PhotoAttachment photo = ((PhotoAttachment) post.attachments.get(i).
                                    getContent());
                            intent.putExtra("original_link", photo.original_url);
                            intent.putExtra("author_id", post.author_id);
                            intent.putExtra("photo_id", photo.id);
                        }
                    }
                }
                ctx.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void openWallComments(Context ctx, int position, View view) {
            OpenVKAPI ovk_api = null;
            SharedPreferences global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            } else {
                return;
            }
            if(ovk_api.account != null) {
                WallPost item;
                Intent intent = new Intent(ctx.getApplicationContext(), WallPostActivity.class);
                if (global_prefs.getString("current_screen", "").equals("profile")) {
                    item = ovk_api.wall.getWallItems().get(position);
                    intent.putExtra("where", "wall");
                } else {
                    item = ovk_api.newsfeed.getWallPosts().get(position);
                    intent.putExtra("where", "newsfeed");
                }
                try {
                    intent.putExtra("post_id", item.post_id);
                    intent.putExtra("owner_id", item.owner_id);
                    intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                            ovk_api.account.last_name));
                    intent.putExtra("account_id", ovk_api.account.id);
                    intent.putExtra("post_author_id", item.author_id);
                    intent.putExtra("post_author_name", item.name);
                    intent.putExtra("post_json", item.getJSONString());
                    ctx.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void addLike(Context ctx, int position, String post, View view) {
            WallPost item;
            SharedPreferences global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            OpenVKAPI ovk_api = null;
            NewsfeedFragment newsfeedFragment = null;
            WallLayout wallLayout = null;
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
                newsfeedFragment = ((AppActivity) ctx).newsfeedFragment;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
                ProfileFragment profileFragment = ((ProfileIntentActivity) ctx).profileFragment;
                if(profileFragment.getView() != null) {
                    wallLayout = (profileFragment.getView().findViewById(R.id.wall_layout));
                } else {
                    return;
                }
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
                wallLayout = ((GroupIntentActivity) ctx).findViewById(R.id.wall_layout);
            } else {
                return;
            }
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
                if(wallLayout != null) {
                    wallLayout.select(position, "likes", "add");
                } else {
                    return;
                }
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
                if(newsfeedFragment != null) {
                    newsfeedFragment.select(position, "likes", "add");
                } else {
                    return;
                }
            }
            ovk_api.likes.add(ovk_api.wrapper, item.owner_id, item.post_id, position);
        }

        public void deleteLike(Context ctx, int position, String post, View view) {
            WallPost item;
            SharedPreferences global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            OpenVKAPI ovk_api = null;
            NewsfeedFragment newsfeedFragment = null;
            WallLayout wallLayout = null;
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
                newsfeedFragment = ((AppActivity) ctx).newsfeedFragment;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
                ProfileFragment profileFragment = ((ProfileIntentActivity) ctx).profileFragment;
                if(profileFragment.getView() != null) {
                    wallLayout = (profileFragment.getView().findViewById(R.id.wall_layout));
                } else {
                    return;
                }
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
                wallLayout = ((GroupIntentActivity) ctx).findViewById(R.id.wall_layout);
            } else {
                return;
            }
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
                if(wallLayout != null) {
                    wallLayout.select(0, "likes", "delete");
                } else {
                    return;
                }
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
                if(newsfeedFragment != null) {
                    newsfeedFragment.select(0, "likes", "delete");
                } else {
                    return;
                }
            }
            ovk_api.likes.delete(ovk_api.wrapper, item.owner_id, item.post_id, position);
        }

        public void showAuthorPage(Context ctx, String where, int position) {
            WallPost item;
            SharedPreferences global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            OpenVKAPI ovk_api = null;
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            } else {
                return;
            }

            if (where.equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
            }

            if(item.author_id != ovk_api.account.id) {
                String url = "";
                if (item.author_id < 0) {
                    url = "openvk://group/" + "club" + -item.author_id;
                } else {
                    url = "openvk://profile/" + "id" + item.author_id;
                }
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage("uk.openvk.android.legacy");
                i.setData(Uri.parse(url));
                ctx.startActivity(i);
            } else {
                if(ctx instanceof AppActivity) {
                    ((AppActivity) ctx).openAccountProfile();
                }
            }
        }

        public void openWallRepostComments(Context ctx, int position, View view) {
            WallPost item;
            Intent intent = new Intent(ctx.getApplicationContext(), WallPostActivity.class);
            SharedPreferences global_prefs = null;
            OpenVKAPI ovk_api = null;
            global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            } else {
                return;
            }
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
                intent.putExtra("where", "wall");
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
                intent.putExtra("where", "newsfeed");
            }
            intent.putExtra("where", "wall");
            try {
                intent.putExtra("post_id", item.repost.newsfeed_item.post_id);
                intent.putExtra("owner_id", item.repost.newsfeed_item.owner_id);
                intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                        ovk_api.account.last_name));
                intent.putExtra("account_id", ovk_api.account.id);
                intent.putExtra("post_author_id", item.repost.newsfeed_item.author_id);
                intent.putExtra("post_author_name", item.repost.newsfeed_item.name);
                intent.putExtra("post_json", item.repost.newsfeed_item.getJSONString());
                ctx.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    public void setArray(ArrayList<WallPost> array) {
        items = array;
    }
}