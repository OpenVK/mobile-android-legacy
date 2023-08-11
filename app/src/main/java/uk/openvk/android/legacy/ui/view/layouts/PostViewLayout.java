package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;
import uk.openvk.android.legacy.api.entities.Comment;
import uk.openvk.android.legacy.api.entities.OvkLink;
import uk.openvk.android.legacy.ui.core.activities.VideoPlayerActivity;
import uk.openvk.android.legacy.ui.core.activities.WallPostActivity;
import uk.openvk.android.legacy.ui.list.adapters.CommentsListAdapter;
import uk.openvk.android.legacy.api.entities.WallPost;

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

public class PostViewLayout extends LinearLayout {
    private final String instance;
    private View headerView;
    private int param = 0;
    public TextView titlebar_title;
    public String state;
    public JSONArray newsfeed;
    public String send_request;
    public SharedPreferences global_prefs;
    private CommentsListAdapter commentsAdapter;
    private RecyclerView commentsView;
    private LinearLayoutManager llm;
    private ArrayList<Comment> comments;

    public PostViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_post_view, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        instance = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("current_instance", "");
        global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    public void createAdapter(Context ctx, ArrayList<Comment> comments) {
        TextView no_comments_text = findViewById(R.id.no_comments_text);
        this.comments = comments;
        commentsAdapter = new CommentsListAdapter(ctx, comments);
        commentsView = (RecyclerView) findViewById(R.id.comments_list);
        if(comments.size() > 0) {
            no_comments_text.setVisibility(GONE);
            commentsView.setVisibility(VISIBLE);
        } else {
            no_comments_text.setVisibility(VISIBLE);
            commentsView.setVisibility(GONE);
        }
        llm = new LinearLayoutManager(ctx);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        commentsView.setLayoutManager(llm);
        commentsView.setAdapter(commentsAdapter);
    }

    public void updateItem(int position) {
        if(commentsAdapter != null) {
            commentsView = (RecyclerView) findViewById(R.id.comments_list);
            commentsAdapter.notifyItemChanged(position);
        }
    }

    public void updateAllItems() {
        if(commentsAdapter != null) {
            commentsView = (RecyclerView) findViewById(R.id.comments_list);
            commentsAdapter.notifyDataSetChanged();
        }
    }


    public int getCount() {
        try {
            return commentsView.getAdapter().getItemCount();
        } catch (NullPointerException npE) {
            return 0;
        }
    }

    public void loadAvatars() {
        if(commentsAdapter != null) {
            commentsView = (RecyclerView) findViewById(R.id.comments_list);
            for (int i = 0; i < getCount(); i++) {
                try {
                    Comment item = comments.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/comment_avatars/avatar_%s",
                                    getContext().getCacheDir(), instance, item.author_id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    } else {
                        Log.e(OvkApplication.APP_TAG, String.format(
                                "'%s/%s/photos_cache/comment_avatars/avatar_%d' not found",
                                getContext().getCacheDir(), instance, item.author_id));
                    }
                    comments.set(i, item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            commentsAdapter.notifyDataSetChanged();
        }
    }

    public void setPost(WallPost item, final Context ctx) {
        ((TextView) findViewById(R.id.wall_view_poster_name)).setText(item.name);
        if(!item.is_explicit || !global_prefs.getBoolean("safeViewing", true)) {
            if (item.text.length() > 0) {
                String text = item.text;
                Pattern pattern = Pattern.compile("\\[(.+?)\\]|" +
                        "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]" +
                        "{1,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
                Matcher matcher = pattern.matcher(text);
                boolean regexp_search = matcher.find();
                int regexp_results = 0;
                text = item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
                while (regexp_search) {
                    if (regexp_results == 0) {
                        text = text.replace("\n", "<br>");
                    }
                    String block = matcher.group();
                    if (block.startsWith("[") && block.endsWith("]")) {
                        OvkLink link = new OvkLink();
                        String[] markup = block.replace("[", "").replace("]", "").split("\\|");
                        link.screen_name = markup[0];
                        if (markup.length == 2) {
                            if (markup[0].startsWith("id")) {
                                link.url = String.format("openvk://profile/%s", markup[0]);
                                link.name = markup[1];
                            } else if (markup[0].startsWith("club")) {
                                link.url = String.format("openvk://group/%s", markup[0]);
                                link.name = markup[1];
                            }
                            link.name = markup[1];
                            if (markup[0].startsWith("id") || markup[0].startsWith("club")) {
                                text = text.replace(block, String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                            }
                        }
                    } else if (block.startsWith("https://") || block.startsWith("http://")) {
                        text = text.replace(block, String.format("<a href=\"%s\">%s</a>", block, block));
                    }
                    regexp_results = regexp_results + 1;
                    regexp_search = matcher.find();
                }
                if (regexp_results > 0) {
                    ((TextView) findViewById(R.id.post_view)).setAutoLinkMask(0);
                    ((TextView) findViewById(R.id.post_view)).setText(Html.fromHtml(text));
                } else {
                    ((TextView) findViewById(R.id.post_view)).setText(text);
                }
                ((TextView) findViewById(R.id.post_view)).setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                ((TextView) findViewById(R.id.post_view)).setVisibility(GONE);
            }
            ((TextView) findViewById(R.id.wall_view_time)).setText(item.info);

            if (item.avatar != null) {
                ((ImageView) findViewById(R.id.wall_user_photo)).setImageBitmap(item.avatar);
            }

            LinearLayout repost_info = ((LinearLayout) findViewById(R.id.post_attach_container));

            if (item.repost != null) {
                TextView original_poster_name = ((TextView) findViewById(R.id.post_retweet_name));
                TextView original_post_info = ((TextView) findViewById(R.id.post_retweet_time));
                TextView original_post_text = ((TextView) findViewById(R.id.post_retweet_text));
                TextView repost_expand_text_btn = ((TextView) findViewById(R.id.repost_expand_text_btn));
                ImageView original_post_photo = (ImageView) findViewById(R.id.repost_photo);
                PollAttachView original_post_poll = (PollAttachView) findViewById(R.id.repost_poll_layout);
                repost_info.setVisibility(View.VISIBLE);
                original_poster_name.setText(item.repost.newsfeed_item.name);
                original_post_info.setText(item.repost.newsfeed_item.info);
                original_post_text.setText(item.repost.newsfeed_item.text);
                original_post_text.setMovementMethod(LinkMovementMethod.getInstance());
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
                } else if (item.repost.newsfeed_item.text.length() > 500) {
                    original_post_text.setText(String.format("%s...",
                            item.repost.newsfeed_item.text.substring(0, 500)));
                    repost_expand_text_btn.setVisibility(View.VISIBLE);
                } else {
                    original_post_text.setText(item.repost.newsfeed_item.text);
                    repost_expand_text_btn.setVisibility(View.GONE);
                }
                repost_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((WallPostActivity) ctx).openWallRepostComments();
                    }
                });
            } else {
                repost_info.setVisibility(View.GONE);
            }

            PollAttachView pollAttachView = findViewById(R.id.poll_layout);
        } else {
            TextView error_label = findViewById(R.id.error_label);
            error_label.setText(ctx.getResources().getString(R.string.post_load_nsfw));
            error_label.setVisibility(View.VISIBLE);
            (findViewById(R.id.post_view)).setVisibility(GONE);
        }

        if (item.counters != null) {
            ((TextView) findViewById(R.id.wall_view_like)).setText(String.format("%s", item.counters.likes));
        }

    }

    public void loadWallAvatar(long author_id, String where) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;
            if(where.equals("newsfeed")) {
                bitmap = BitmapFactory.decodeFile(String.format("%s/%s/photos_cache/newsfeed_avatars/avatar_%s",
                        getContext().getCacheDir(), instance, author_id), options);
            } else {
                bitmap = BitmapFactory.decodeFile(String.format("%s/%s/photos_cache/wall_avatars/avatar_%s",
                        getContext().getCacheDir(), instance, author_id), options);
            }
            if (bitmap != null) {
                ((ImageView) findViewById(R.id.wall_user_photo)).setImageBitmap(bitmap);
                Log.e(OvkApplication.APP_TAG, String.format("'%s/%s/photos_cache/wall_avatars/avatar_%d' not found",
                        getContext().getCacheDir(), instance, author_id));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadWallPhoto(WallPost post, String where) {
        if(!post.is_explicit || !global_prefs.getBoolean("safeViewing", true)) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = null;
                Bitmap repost_bitmap = null;
                boolean post_attachment_not_loaded = false;
                boolean repost_attachment_not_loaded = false;
                if (post.repost != null) {
                    if(post.repost.newsfeed_item.attachments != null) {
                        for (int i = 0; i < post.repost.newsfeed_item.attachments.size(); i++) {
                            Attachment attachment = post.repost.newsfeed_item.attachments.get(i);
                            if (attachment.status.equals("loading") && attachment.type.equals("photo")) {
                                repost_attachment_not_loaded = true;
                            }
                        }
                    }
                    if(!repost_attachment_not_loaded) {
                        if (where.equals("newsfeed")) {
                            repost_bitmap = BitmapFactory.decodeFile(
                                    String.format("%s/%s/photos_cache/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s",
                                            getContext().getCacheDir(), instance, post.repost.newsfeed_item.owner_id,
                                            post.repost.newsfeed_item.post_id), options);
                        } else {
                            repost_bitmap = BitmapFactory.decodeFile(
                                    String.format("%s/%s/photos_cache/wall_photo_attachments/wall_attachment_o%sp%s",
                                            getContext().getCacheDir(), instance, post.repost.newsfeed_item.owner_id,
                                            post.repost.newsfeed_item.post_id), options);
                        }
                    }
                }
                if(post.attachments != null) {
                    for (int i = 0; i < post.attachments.size(); i++) {
                        Attachment attachment = post.attachments.get(i);
                        if (attachment.status.equals("loading") && attachment.type.equals("photo")) {
                            post_attachment_not_loaded = true;
                        }
                    }
                }
                if(post_attachment_not_loaded) {
                    if (where.equals("newsfeed")) {
                        bitmap = BitmapFactory.decodeFile(
                                String.format("%s/%s/photos_cache/newsfeed_photo_attachments/newsfeed_attachment_o%sp%s",
                                        getContext().getCacheDir(), instance, post.owner_id, post.post_id), options);
                    } else {
                        bitmap = BitmapFactory.decodeFile(
                                String.format("%s/%s/photos_cache/wall_photo_attachments/wall_attachment_o%sp%s",
                                        getContext().getCacheDir(), instance, post.owner_id, post.post_id), options);
                    }
                }
                final ImageView post_photo = ((ImageView) findViewById(R.id.post_photo));
                final ImageView repost_photo = ((ImageView) findViewById(R.id.repost_photo));
                if (bitmap != null) {
                    final float aspect_ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                    post_photo.setImageBitmap(bitmap);
                    post_photo.setVisibility(View.VISIBLE);
                } else {
                    Log.e(OvkApplication.APP_TAG,
                            String.format("'%s/%s/photos_cache/wall_photo_attachments/wall_attachment_o%sp%s' not found",
                                    getContext().getCacheDir(), instance, post.owner_id, post.post_id));
                    post_photo.setVisibility(GONE);
                }
                if (repost_bitmap != null) {
                    repost_photo.setImageBitmap(repost_bitmap);
                    repost_photo.setVisibility(View.VISIBLE);
                    post_photo.setVisibility(GONE);
                }
            } catch (OutOfMemoryError error) {
                Log.e("OpenVK Legacy", "Bitmap error: Out of memory");
            } catch (Exception ex) {
                Log.e("OpenVK Legacy", String.format("Bitmap error: %s", ex.getMessage()));
            }
        }
    }

    public void loadVideoAttachment(final Context ctx, final VideoAttachment video, final long owner_id) {
        final VideoAttachView post_video = findViewById(R.id.post_video);
        post_video.setAttachment(video);
        post_video.setVisibility(View.VISIBLE);
        post_video.setThumbnail(owner_id);
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
                intent.putExtra("title", video.title);
                intent.putExtra("attachment", video);
                intent.putExtra("files", video.files);
                intent.putExtra("owner_id", owner_id);
                ctx.startActivity(intent);
            }
        });
    }

    public void adjustLayoutSize(int orientation) {
        if (((OvkApplication) getContext().getApplicationContext()).isTablet) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout.LayoutParams layoutParams = new LayoutParams((int)
                        (600 * (getResources().getDisplayMetrics().density)), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ((LinearLayout) findViewById(R.id.post_with_comments_view_ll)).setLayoutParams(layoutParams);
            } else {
                LinearLayout.LayoutParams layoutParams = new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ((LinearLayout) findViewById(R.id.post_with_comments_view_ll)).setLayoutParams(layoutParams);
            }
        } else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout.LayoutParams layoutParams = new LayoutParams((int)
                        (480 * (getResources().getDisplayMetrics().density)), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ((LinearLayout) findViewById(R.id.post_with_comments_view_ll)).setLayoutParams(layoutParams);
            } else {
                LinearLayout.LayoutParams layoutParams = new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ((LinearLayout) findViewById(R.id.post_with_comments_view_ll)).setLayoutParams(layoutParams);
            }
        }
    }

    public void setPhotoListener(final Context ctx) {
        ((ImageView) findViewById(R.id.post_photo)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
                    ((WallPostActivity) ctx).viewPhotoAttachment();
                }
            }
        });
        ((ImageView) findViewById(R.id.repost_photo)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("WallPostActivity")) {
                    ((WallPostActivity) ctx).viewPhotoAttachment();
                }
            }
        });
    }

    public void loadPhotos() {
        if(commentsAdapter != null) {
            commentsView = (RecyclerView) findViewById(R.id.comments_list);
            for (int i = 0; i < getCount(); i++) {
                try {
                    Comment item = comments.get(i);
                    if(item.attachments.size() > 0) {
                        for (int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                            if (item.attachments.get(attachment_index).type.equals("photo")) {
                                PhotoAttachment photoAttachment = ((PhotoAttachment) item.attachments.get(0).getContent());
                                Attachment attachment = item.attachments.get(0);
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                if (photoAttachment.url.length() > 0) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(
                                            String.format("%s/%s/photos_cache/comment_photos/comment_photo_o%sp%s",
                                                    getContext().getCacheDir(), instance, item.author_id, item.id), options);
                                    if (bitmap != null) {
                                        photoAttachment.photo = bitmap;
                                        attachment.status = "done";
                                        item.attachments.set(i, attachment);
                                    } else if (photoAttachment.url.length() > 0) {
                                        Log.e(OvkApplication.APP_TAG, "Loading photo error in comments");
                                        attachment.status = "error";
                                    }
                                }
                            }
                        }
                    } else {
                        item.attachments.get(0).status = "not_supported";
                    }
                    comments.set(i, item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            commentsAdapter.notifyDataSetChanged();
        }
    }

    public void loadPollAttachment(Context ctx, PollAttachment poll) {
        PollAttachView pollAttachView = findViewById(R.id.poll_layout);
        pollAttachView.createAdapter(ctx, 0, poll.answers, poll.multiple, poll.user_votes, poll.votes);
        pollAttachView.setPollInfo(poll.question, poll.anonymous, poll.end_date);
        pollAttachView.setVisibility(View.VISIBLE);
    }
}