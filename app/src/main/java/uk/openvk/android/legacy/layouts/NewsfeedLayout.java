package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.list_adapters.NewsfeedAdapter;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

public class NewsfeedLayout extends LinearLayout {
    private View headerView;
    private int param = 0;
    public TextView titlebar_title;
    public String state;
    public JSONArray newsfeed;
    public String send_request;
    public SharedPreferences global_sharedPreferences;
    private NewsfeedAdapter newsfeedAdapter;
    private RecyclerView newsfeedView;
    private ListView newsfeedListView;
    private LinearLayoutManager llm;
    private ArrayList<NewsfeedItem> newsfeedItems;

    public NewsfeedLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.newsfeed_layout, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void createAdapter(Context ctx, ArrayList<NewsfeedItem> newsfeedItems) {
        this.newsfeedItems = newsfeedItems;
        newsfeedAdapter = new NewsfeedAdapter(ctx, newsfeedItems);
        newsfeedView = (RecyclerView) findViewById(R.id.news_listview);
        llm = new LinearLayoutManager(ctx);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        newsfeedView.setLayoutManager(llm);
        newsfeedView.setAdapter(newsfeedAdapter);
    }

    public void updateItem(int position) {
        if(newsfeedAdapter != null) {
            newsfeedView = (RecyclerView) findViewById(R.id.news_listview);
            newsfeedAdapter.notifyItemChanged(position);
        }
    }

    public void updateAllItems() {
        if(newsfeedAdapter != null) {
            newsfeedView = (RecyclerView) findViewById(R.id.news_listview);
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    public void loadAvatars() {
        if(newsfeedAdapter != null) {
            newsfeedView = (RecyclerView) findViewById(R.id.news_listview);
            for (int i = 0; i < getCount(); i++) {
                try {
                    NewsfeedItem item = newsfeedItems.get(i);
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/newsfeed_avatars/avatar_%d", getContext().getCacheDir(), item.author_id), options);
                    if (bitmap != null) {
                        item.avatar = bitmap;
                    }
                    newsfeedItems.set(i, item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    private void loadPhotos() {
        newsfeedView = (RecyclerView) findViewById(R.id.news_listview);
        try {
            int visibleItemCount = llm.getChildCount();
            int totalItemCount = llm.getItemCount();
            int firstVisibleItemPosition = llm.findFirstVisibleItemPosition();
            int lastVisibleItemPosition = llm.findLastVisibleItemPosition();
            for (int i = 0; i < totalItemCount; i++) {
                try {
                    NewsfeedItem item = newsfeedItems.get(i);
                    if (i < firstVisibleItemPosition || i > lastVisibleItemPosition) {
                        item.photo = null;
                    } else {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/newsfeed_photo_attachments/newsfeed_attachment_%s", getContext().getCacheDir(), item.post_id), options);
                        if (bitmap != null) {
                            item.photo = bitmap;
                            item.photo_status = "loaded";
                        } else if(item.photo_hsize_url.length() > 0 || item.photo_msize_url.length() > 0) {
                            item.photo_status = "error";
                        }
                    }
                    newsfeedItems.set(i, item);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        newsfeedAdapter.notifyDataSetChanged();
    }

    public void setScrollingPositions() {
        loadPhotos();
        newsfeedView.setOnScrollListener(new RecyclerView.OnScrollListener() {
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


    public int getCount() {
        try {
            return newsfeedView.getAdapter().getItemCount();
        } catch (NullPointerException npE) {
            return 0;
        }
    }

    public void select(int position, String item, int value) {
        if(item.equals("likes")) {
            if(value == 1) {
                newsfeedItems.get(position).counters.isLiked = true;
                newsfeedItems.get(position).counters.likes += 1;
            } else {
                newsfeedItems.get(position).counters.isLiked = false;
                newsfeedItems.get(position).counters.likes -= 1;
            }
            newsfeedAdapter.notifyDataSetChanged();
        }
    }

    public void adjustLayoutSize(int orientation) {
        if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            newsfeedView = (RecyclerView) findViewById(R.id.news_listview);
            LinearLayout.LayoutParams layoutParams = new LayoutParams((int)(600 * (getResources().getDisplayMetrics().density)), ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            newsfeedView.setLayoutParams(layoutParams);
        } else {
            newsfeedView = (RecyclerView) findViewById(R.id.news_listview);
            LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            newsfeedView.setLayoutParams(layoutParams);
        }
    }
}