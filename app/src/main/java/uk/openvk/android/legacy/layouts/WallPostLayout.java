package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Comment;
import uk.openvk.android.legacy.list_adapters.CommentsListAdapter;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

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

    public void setPost(NewsfeedItem item) {
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

        if(item.photo != null) {
            ((ImageView) findViewById(R.id.post_photo)).setImageBitmap(item.photo);
            ((ImageView) findViewById(R.id.post_photo)).setVisibility(VISIBLE);
        } else {
            ((ImageView) findViewById(R.id.post_photo)).setVisibility(GONE);
        }
        ((TextView) findViewById(R.id.wall_view_like)).setText("" + item.counters.likes);
        if(item.counters.isLiked) {
            ((TextView) findViewById(R.id.wall_view_like)).setSelected(true);
        }
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

    public void loadWallPhoto(int post_id, String where) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = null;
            if(where.equals("newsfeed")) {
                bitmap = BitmapFactory.decodeFile(String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_%s", getContext().getCacheDir(), post_id), options);
            } else {
                bitmap = BitmapFactory.decodeFile(String.format("%s/wall_photo_attachments/wall_attachment_%s", getContext().getCacheDir(), post_id), options);
            }
            if (bitmap != null) {
                ((ImageView) findViewById(R.id.post_photo)).setImageBitmap(bitmap);
                ((ImageView) findViewById(R.id.post_photo)).setVisibility(VISIBLE);
            } else {
                ((ImageView) findViewById(R.id.post_photo)).setVisibility(GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}