package uk.openvk.android.legacy.user_interface.view.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.api.models.OvkLink;
import uk.openvk.android.legacy.user_interface.core.activities.WallPostActivity;
import uk.openvk.android.legacy.user_interface.list.adapters.CommentsListAdapter;
import uk.openvk.android.legacy.api.models.WallPost;

public class PostViewLayout extends LinearLayout {
    private View headerView;
    private int param = 0;
    public TextView titlebar_title;
    public String state;
    public JSONArray newsfeed;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private CommentsListAdapter commentsAdapter;
    private RecyclerView commentsView;
    private LinearLayoutManager llm;
    private ArrayList<Comment> comments;

    public PostViewLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.post_view_layout, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
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
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/comment_avatars/avatar_%d", getContext().getCacheDir(), item.author_id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    } else {
                        Log.e("OpenVK", String.format("'%s/photos_cache/comment_avatars/avatar_%d' not found", getContext().getCacheDir(), item.author_id));
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
        if(item.text.length() > 0) {
            String text = item.text;
            Pattern pattern = Pattern.compile("\\[(.+?)\\]|" +
                    "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{1,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
            Matcher matcher = pattern.matcher(text);
            boolean regexp_search = matcher.find();
            int regexp_results = 0;
            text = item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                    .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
            while(regexp_search) {
                if(regexp_results == 0) {
                    text = text.replace("\n", "<br>");
                }
                String block = matcher.group();
                if(block.startsWith("[") && block.endsWith("]")) {
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
                } else if(block.startsWith("https://") || block.startsWith("http://")) {
                    text = text.replace(block, String.format("<a href=\"%s\">%s</a>", block, block));
                }
                regexp_results = regexp_results + 1;
                regexp_search = matcher.find();
            }
            if(regexp_results > 0) {
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

        if(item.avatar != null) {
            ((ImageView) findViewById(R.id.wall_user_photo)).setImageBitmap(item.avatar);
        }

        LinearLayout repost_info = ((LinearLayout) findViewById(R.id.post_attach_container));

        if(item.repost != null) {
            TextView original_poster_name = ((TextView) findViewById(R.id.post_retweet_name));
            TextView original_post_info = ((TextView) findViewById(R.id.post_retweet_time));
            TextView original_post_text = ((TextView) findViewById(R.id.post_retweet_text));
            TextView repost_expand_text_btn = ((TextView) findViewById(R.id.repost_expand_text_btn));
            ImageView original_post_photo = (ImageView) findViewById(R.id.repost_photo);
            PollLayout original_post_poll = (PollLayout) findViewById(R.id.repost_poll_layout);
            repost_info.setVisibility(View.VISIBLE);
            original_poster_name.setText(item.repost.newsfeed_item.name);
            original_post_info.setText(item.repost.newsfeed_item.info);
            original_post_text.setText(item.repost.newsfeed_item.text);
            original_post_text.setMovementMethod(LinkMovementMethod.getInstance());
            String[] repost_lines = item.repost.newsfeed_item.text.split("\r\n|\r|\n");
            if(repost_lines.length > 8 && item.repost.newsfeed_item.text.length() <= 500) {
                String text_llines = "";
                for(int line_no = 0; line_no < 8; line_no++) {
                    if(line_no == 7) {
                        text_llines += String.format("%s...", repost_lines[line_no]);
                    } else {
                        text_llines += String.format("%s\r\n", repost_lines[line_no]);
                    }
                }
                original_post_text.setText(text_llines);
                repost_expand_text_btn.setVisibility(View.VISIBLE);
            } else if(item.repost.newsfeed_item.text.length() > 500) {
                original_post_text.setText(String.format("%s...", item.repost.newsfeed_item.text.substring(0, 500)));
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

        if(item.counters != null) {
            ((TextView) findViewById(R.id.wall_view_like)).setText(String.format("%s", item.counters.likes));
        }

        PollLayout pollLayout = findViewById(R.id.poll_layout);

    }

    public void loadWallAvatar(long author_id, String where) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;
            if(where.equals("newsfeed")) {
                bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/newsfeed_avatars/avatar_%d", getContext().getCacheDir(), author_id), options);
            } else {
                bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/wall_avatars/avatar_%d", getContext().getCacheDir(), author_id), options);
            }
            if (bitmap != null) {
                ((ImageView) findViewById(R.id.wall_user_photo)).setImageBitmap(bitmap);
                Log.e("OpenVK", String.format("'%s/photos_cache/wall_avatars/avatar_%d' not found", getContext().getCacheDir(), author_id));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void loadWallPhoto(WallPost post, String where) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;
            Bitmap repost_bitmap = null;
            if(post.repost != null) {
                if(where.equals("newsfeed")) {
                    repost_bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/newsfeed_photo_attachments/newsfeed_attachment_o%dp%d", getContext().getCacheDir(), post.repost.newsfeed_item.owner_id, post.repost.newsfeed_item.post_id), options);
                } else {
                    repost_bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/wall_photo_attachments/wall_attachment_o%dp%d", getContext().getCacheDir(), post.repost.newsfeed_item.owner_id, post.repost.newsfeed_item.post_id), options);
                }
            }
            if(where.equals("newsfeed")) {
                bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/newsfeed_photo_attachments/newsfeed_attachment_o%dp%d", getContext().getCacheDir(), post.owner_id, post.post_id), options);
            } else {
                bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/wall_photo_attachments/wall_attachment_o%dp%d", getContext().getCacheDir(), post.owner_id, post.post_id), options);
            }
            final ImageView post_photo = ((ImageView) findViewById(R.id.post_photo));
            final ImageView repost_photo = ((ImageView) findViewById(R.id.repost_photo));
            if (bitmap != null) {
                final float aspect_ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                post_photo.setImageBitmap(bitmap);
                post_photo.setVisibility(View.VISIBLE);
            } else {
                Log.e("OpenVK", String.format("'%s/photos_cache/wall_photo_attachments/wall_attachment_o%dp%d' not found", getContext().getCacheDir(), post.owner_id, post.post_id));
                post_photo.setVisibility(GONE);
            }
            if(repost_bitmap != null) {
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

    public void adjustLayoutSize(int orientation) {
        if (((OvkApplication) getContext().getApplicationContext()).isTablet) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout.LayoutParams layoutParams = new LayoutParams((int) (600 * (getResources().getDisplayMetrics().density)), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ((LinearLayout) findViewById(R.id.post_with_comments_view_ll)).setLayoutParams(layoutParams);
            } else {
                LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ((LinearLayout) findViewById(R.id.post_with_comments_view_ll)).setLayoutParams(layoutParams);
            }
        } else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout.LayoutParams layoutParams = new LayoutParams((int) (480 * (getResources().getDisplayMetrics().density)), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                ((LinearLayout) findViewById(R.id.post_with_comments_view_ll)).setLayoutParams(layoutParams);
            } else {
                LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
}