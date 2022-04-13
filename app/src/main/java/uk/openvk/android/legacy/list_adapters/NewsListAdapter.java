package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.list_items.NewsListItem;
import uk.openvk.android.legacy.listeners.SwipeListener;

public class NewsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<NewsListItem> objects;
    public boolean opened_sliding_menu;

    public NewsListAdapter(Context context, ArrayList<NewsListItem> items) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    NewsListItem getNewsListItem(int position) {
        return ((NewsListItem) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.news_item, parent, false);
        }

        final NewsListItem item = getNewsListItem(position);
        if(item.counters.isLiked == true) {
            ((TextView) view.findViewById(R.id.post_likes)).setSelected(true);
        } else {
            ((TextView) view.findViewById(R.id.post_likes)).setSelected(false);
        }

        if(item.counters.isReposted == true) {
            ((TextView) view.findViewById(R.id.post_reposts)).setSelected(true);
        } else {
            ((TextView) view.findViewById(R.id.post_reposts)).setSelected(false);
        }
        ((TextView) view.findViewById(R.id.poster_name_view)).setText(item.name);
        //((TextView) view.findViewById(R.id.post_retweet_name)).setText(((RepostInfo) item.repost).name);
        //((TextView) view.findViewById(R.id.post_retweet_time)).setText(((RepostInfo) item.repost).time);
        ((TextView) view.findViewById(R.id.post_info_view)).setText(item.info);

        if(item.getPhoto() != null) {
            ((ImageView) view.findViewById(R.id.post_photo)).setImageBitmap(item.getPhoto());
            ((ImageView) view.findViewById(R.id.post_photo)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.post_photo)).setVisibility(View.GONE);
        }

        if(item.text.length() > 0) {
            ((TextView) view.findViewById(R.id.post_view)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.post_view)).setText(Html.fromHtml(item.text));
        } else if((item.photo == null) && item.text.length() == 0) {
            ((TextView) view.findViewById(R.id.post_view)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.post_view)).setText(Html.fromHtml("<i>" + ctx.getResources().getString(R.string.not_implemented) + "</i>"));
        } else {
            ((TextView) view.findViewById(R.id.post_view)).setVisibility(View.GONE);
        }
        ((TextView) view.findViewById(R.id.post_view)).setMovementMethod(LinkMovementMethod.getInstance());

//        ((TextView) view.findViewById(R.id.post_retweet_time)).setText(((RepostInfo) item.repost).time);

        ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        });

        view.setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        });

        final LinearLayout news_item_ll = view.findViewById(R.id.news_item_ll);
        final LinearLayout poster_ll = view.findViewById(R.id.poster_ll);
        news_item_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).hideSelectedItemBackground(position);
                } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                    ((ProfileIntentActivity) ctx).hideSelectedItemBackground(position);
                }
            }
        });

        ((TextView) view.findViewById(R.id.post_likes)).setText("" + item.counters.likes);
        ((TextView) view.findViewById(R.id.post_comments)).setText("" + item.counters.comments);
        ((TextView) view.findViewById(R.id.post_reposts)).setText("" + item.counters.reposts);

        ((TextView) view.findViewById(R.id.post_likes)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).addLike(position, "post", view);
                } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                    ((ProfileIntentActivity) ctx).addLike(position, "post", view);
                }
            }
        });
        poster_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item.owner_id > 0 && ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).getOwnerProfile(item);
                }
            }
        });

        return view;
    }

    public class ViewHolder {
        public TextView item_name;
        public TextView item_info;
        public TextView item_text;
        public TextView item_likes_counters;
        public TextView item_comments_counters;
        public TextView item_reposts_counters;
    }

}
