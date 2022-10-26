package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.GroupIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.WallPostActivity;
import uk.openvk.android.legacy.user_interface.list_adapters.CommentsListAdapter;
import uk.openvk.android.legacy.api.models.WallPost;

public class WallPostLayout extends LinearLayout {
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

    public WallPostLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.wall_post_layout, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void createAdapter(Context ctx, ArrayList<Comment> comments) {
        this.comments = comments;
        commentsAdapter = new CommentsListAdapter(ctx, comments);
        commentsView = (RecyclerView) findViewById(R.id.comments_list);
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
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/comment_avatars/avatar_%d", getContext().getCacheDir(), item.id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    comments.set(i, item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            commentsAdapter.notifyDataSetChanged();
        }
    }

    public void setPost(WallPost item) {
        ((TextView) findViewById(R.id.wall_view_poster_name)).setText(item.name);
        if(item.text.length() > 0) {
            ((TextView) findViewById(R.id.post_view)).setText(item.text);
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
            ImageView original_post_photo = (ImageView) findViewById(R.id.repost_photo);
            PollLayout original_post_poll = (PollLayout) findViewById(R.id.repost_poll_layout);
            repost_info.setVisibility(View.VISIBLE);
            original_poster_name.setText(item.repost.newsfeed_item.name);
            original_post_info.setText(item.repost.newsfeed_item.info);
            original_post_text.setText(item.repost.newsfeed_item.text);

            repost_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

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

    public void loadWallAvatar(int author_id, String where) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;
            if(where.equals("newsfeed")) {
                bitmap = BitmapFactory.decodeFile(String.format("%s/newsfeed_avatars/avatar_%d", getContext().getCacheDir(), author_id), options);
            } else {
                bitmap = BitmapFactory.decodeFile(String.format("%s/wall_avatars/avatar_%d", getContext().getCacheDir(), author_id), options);
            }
            if (bitmap != null) {
                ((ImageView) findViewById(R.id.wall_user_photo)).setImageBitmap(bitmap);
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
                    repost_bitmap = BitmapFactory.decodeFile(String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_o%dp%d", getContext().getCacheDir(), post.repost.newsfeed_item.owner_id, post.repost.newsfeed_item.post_id), options);
                } else {
                    repost_bitmap = BitmapFactory.decodeFile(String.format("%s/wall_photo_attachments/wall_attachment_o%dp%d", getContext().getCacheDir(), post.repost.newsfeed_item.owner_id, post.repost.newsfeed_item.post_id), options);
                }
            }
            if(where.equals("newsfeed")) {
                bitmap = BitmapFactory.decodeFile(String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_o%dp%d", getContext().getCacheDir(), post.owner_id, post.post_id), options);
            } else {
                bitmap = BitmapFactory.decodeFile(String.format("%s/wall_photo_attachments/wall_attachment_o%dp%d", getContext().getCacheDir(), post.owner_id, post.post_id), options);
            }
            final ImageView post_photo = ((ImageView) findViewById(R.id.post_photo));
            final ImageView repost_photo = ((ImageView) findViewById(R.id.repost_photo));
            if (bitmap != null) {
                final float aspect_ratio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
                post_photo.setImageBitmap(bitmap);
                post_photo.setVisibility(View.VISIBLE);
            } else {
                Log.e("OpenVK", String.format("'%s/wall_photo_attachments/wall_attachment_o%dp%d' not found", getContext().getCacheDir(), post.owner_id, post.post_id));
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