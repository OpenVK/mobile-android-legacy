package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.list_items.NewsListItem;
import uk.openvk.android.legacy.listeners.SwipeListener;

public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.Holder> {

    private ArrayList<NewsListItem> items = new ArrayList<>();
    private Context ctx;

    public NewsListAdapter(Context context, ArrayList<NewsListItem> posts) {
        ctx = context;
        items = posts;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.news_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    public NewsListItem getItem(int position) {
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
        public final TextView likes_counter;
        public final TextView reposts_counter;
        public final TextView comments_counter;
        public final View convertView;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.poster_name = view.findViewById(R.id.poster_name_view);
            this.post_info = view.findViewById(R.id.post_info_view);
            this.post_text = view.findViewById(R.id.post_view);
            this.post_photo = view.findViewById(R.id.post_photo);
            this.likes_counter = view.findViewById(R.id.post_likes);
            this.reposts_counter = view.findViewById(R.id.post_reposts);
            this.comments_counter = view.findViewById(R.id.post_comments);
        }

        void bind(final int position) {
            NewsListItem item = getItem(position);
            poster_name.setText(item.name);
            post_info.setText(item.info);
            if(item.text.length() > 0) {
                post_text.setVisibility(View.VISIBLE);
                post_text.setText(Html.fromHtml(item.text));
                post_text.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                post_text.setVisibility(View.GONE);
            }

            if(item.counters.isLiked == true) {
                likes_counter.setSelected(true);
            } else {
                likes_counter.setSelected(false);
            }

            likes_counter.setText("" + item.counters.likes);
            reposts_counter.setText("" + item.counters.reposts);
            comments_counter.setText("" + item.counters.comments);
            Bitmap item_photo = item.getPhoto();
            if(item_photo != null) {
                post_photo.setImageBitmap(item_photo);
                post_photo.setVisibility(View.VISIBLE);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) post_photo.getLayoutParams();
                layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                post_photo.setLayoutParams(layoutParams);
            } else {
                post_photo.setVisibility(View.GONE);
            }

            post_text.setOnTouchListener(new SwipeListener(ctx) {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        if (((AppActivity) ctx).menu_is_closed == false) {
                            ((AppActivity) ctx).openSlidingMenu();
                        }
                    }
                    return super.onTouch(v, event);
                }
            });
            post_photo.setOnTouchListener(new SwipeListener(ctx) {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        if (((AppActivity) ctx).menu_is_closed == false) {
                            ((AppActivity) ctx).openSlidingMenu();
                        }
                    }
                    return super.onTouch(v, event);
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        if (((AppActivity) ctx).menu_is_closed == false) {
                            ((AppActivity) ctx).openSlidingMenu();
                        }
                    }
                }
            });
            likes_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        if(((AppActivity) ctx).menu_is_closed == false) {
                            ((AppActivity) ctx).openSlidingMenu();
                        } else {
                            ((AppActivity) ctx).addLike(position, "post", view);
                        }
                    } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).addLike(position, "post", view);
                    }
                }
            });
        }
    }

    public void setArray(ArrayList<NewsListItem> array) {
        items = array;
    }
}