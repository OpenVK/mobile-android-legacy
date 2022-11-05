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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.user_interface.list_adapters.NewsfeedAdapter;
import uk.openvk.android.legacy.api.models.WallPost;

public class WallLayout extends LinearLayout {
    private View headerView;
    private int param = 0;
    public TextView titlebar_title;
    public String state;
    public JSONArray wall;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private NewsfeedAdapter wallAdapter;
    private RecyclerView wallView;
    private LinearLayoutManager llm;
    private ArrayList<WallPost> wallItems;

    public WallLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.wall_layout, null);

        this.addView(view);

        llm = new LinearLayoutManager(context);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int heightMeasureSpec_custom = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec_custom);
        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }

    public void createAdapter(Context ctx, ArrayList<WallPost> wallItems) {
        this.wallItems = wallItems;
        wallAdapter = new NewsfeedAdapter(ctx, wallItems);
        wallView = (RecyclerView) findViewById(R.id.wall_listview);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        wallView.setLayoutManager(llm);
        wallView.setAdapter(wallAdapter);
    }

    public void updateItem(WallPost item, int position) {
        if(wallAdapter != null) {
            wallView = (RecyclerView) findViewById(R.id.news_listview);
            wallItems.set(position, item);
            wallAdapter.notifyItemChanged(position);
        }
    }

    private void loadPhotos() {
        wallView = (RecyclerView) findViewById(R.id.wall_listview);
        try {
            if(wallAdapter != null) {
                int visibleItemCount = llm.getChildCount();
                int totalItemCount = llm.getItemCount();
                int firstVisibleItemPosition = llm.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = llm.findLastVisibleItemPosition();
                for (int i = 0; i < totalItemCount; i++) {
                    WallPost item = wallItems.get(i);
                    try {
                        if(item.repost != null) {
                            if (item.repost.newsfeed_item.attachments.size() > 0) {
                                if (item.repost.newsfeed_item.attachments.get(0).type.equals("photo")) {
                                    PhotoAttachment photoAttachment = ((PhotoAttachment) item.repost.newsfeed_item.attachments.get(0).getContent());
                                    Attachment attachment = item.repost.newsfeed_item.attachments.get(0);
                                    if (i < firstVisibleItemPosition || i > lastVisibleItemPosition) {
                                        if(photoAttachment.photo != null) {
                                            photoAttachment.photo.recycle();
                                            photoAttachment.photo = null;
                                            System.gc();
                                        }
                                    } else {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                        if (photoAttachment.url.length() > 0) {
                                            Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/wall_photo_attachments/wall_attachment_o%dp%d", getContext().getCacheDir(),
                                                    item.repost.newsfeed_item.owner_id, item.repost.newsfeed_item.post_id), options);
                                            if (bitmap != null) {
                                                photoAttachment.photo = bitmap;
                                                attachment.status = "done";
                                                item.repost.newsfeed_item.attachments.set(0, attachment);
                                            }
                                        }
                                    }
                                    wallItems.set(i, item);
                                }
                            }
                        }
                        if (i < firstVisibleItemPosition || i > lastVisibleItemPosition) {
                            if(item.attachments.get(0).type.equals("photo")) {
                                ((PhotoAttachment) item.attachments.get(0).getContent()).photo = null;
                            }
                        } else {
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            if(item.attachments.size() > 0) {
                                if(item.attachments.get(0).type.equals("photo")) {
                                    if (((PhotoAttachment) item.attachments.get(0).getContent()).url.length() > 0) {
                                        Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/wall_photo_attachments/wall_attachment_o%dp%d", getContext().getCacheDir(), item.owner_id, item.post_id), options);
                                        if (bitmap != null) {
                                            ((PhotoAttachment) item.attachments.get(0).getContent()).photo = bitmap;
                                            item.attachments.get(0).status = "done";
                                        }
                                    }
                                }
                            }
                        }
                        wallItems.set(i, item);
                    } catch (OutOfMemoryError error) {
                        Log.e("OpenVK Legacy", "Bitmap error: Out of memory");
                    } catch (Exception ex) {
                        if(ex.getMessage() != null) {
                            Log.e("OpenVK Legacy", String.format("Bitmap error: %s", ex.getMessage()));
                        } else {
                            Log.e("OpenVK Legacy", String.format("Bitmap error: %s", ex.getClass().getSimpleName()));
                        }
                    }
                }
                wallAdapter.notifyDataSetChanged();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setScrollingPositions() {
        loadPhotos();
        wallView = (RecyclerView) findViewById(R.id.wall_listview);
        wallView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                loadPhotos();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

            }
        });
    }

    public void select(int position, String item, int value) {
        if(item.equals("likes")) {
            if(value == 1) {
                wallItems.get(position).counters.isLiked = true;
            } else {
                wallItems.get(position).counters.isLiked = false;
            }
            wallAdapter.notifyDataSetChanged();
        }
    }

    public void select(int position, String item, String value) {
        if(item.equals("likes")) {
            if(value.equals("add")) {
                wallItems.get(position).counters.isLiked = true;
            } else {
                wallItems.get(position).counters.isLiked = false;
            }
            wallAdapter.notifyDataSetChanged();
        }
    }

    public void limitWidth(final int width) {
        this.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int leftWas, int topWas, int rightWas, int bottomWas )
            {
                int widthWas = rightWas - leftWas;
                if( v.getWidth() != widthWas ) {
                    if (v.getWidth() > width) {
                        wallView.getLayoutParams().width = width;
                    }
                }
            }
        });
    }

    public void loadAvatars() {
        if(wallAdapter != null) {
            wallView = (RecyclerView) findViewById(R.id.wall_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    WallPost item = wallItems.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/wall_avatars/avatar_%d", getContext().getCacheDir(), item.author_id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    wallItems.set(i, item);
                } catch (OutOfMemoryError err) {
                    err.printStackTrace();
                }
            }
            wallAdapter.notifyDataSetChanged();
        }
    }

    private int getCount() {
        try {
            return wallView.getAdapter().getItemCount();
        } catch (NullPointerException npE) {
            return 0;
        }
    }

    public void adjustLayoutSize(int orientation) {
        if (((OvkApplication) getContext().getApplicationContext()).isTablet) {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                wallView = (RecyclerView) findViewById(R.id.wall_listview);
                LinearLayout.LayoutParams layoutParams = new LayoutParams((int) (600 * (getResources().getDisplayMetrics().density)), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                wallView.setLayoutParams(layoutParams);
            } else {
                wallView = (RecyclerView) findViewById(R.id.wall_listview);
                LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                wallView.setLayoutParams(layoutParams);
            }
        } else {
            wallView = (RecyclerView) findViewById(R.id.wall_listview);
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                LinearLayout.LayoutParams layoutParams = new LayoutParams((int) (480 * (getResources().getDisplayMetrics().density)), ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                wallView.setLayoutParams(layoutParams);
            } else {
                LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                wallView.setLayoutParams(layoutParams);
            }
        }
    }
}