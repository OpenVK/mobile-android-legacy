package uk.openvk.android.legacy.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.items.SearchResultItem;
import uk.openvk.android.legacy.listeners.SwipeListener;

public class SearchResultAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<SearchResultItem> objects;
    public boolean opened_sliding_menu;

    SearchResultAdapter(Context context, ArrayList<SearchResultItem> items) {
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

    SearchResultItem getFriendsListItem(int position) {
        return ((SearchResultItem) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.search_result_item, parent, false);
        }

        SearchResultItem item = getFriendsListItem(position);
        ((TextView) view.findViewById(R.id.sr_list_item_text)).setText(item.title);
        if(item.subtitle.length() > 0) {
            ((TextView) view.findViewById(R.id.sr_list_item_subtext)).setText(item.subtitle);
        } else {
            ((TextView) view.findViewById(R.id.sr_list_item_subtext)).setVisibility(View.GONE);
        }
        if(item.online == 1) {
            ((ImageView) view.findViewById(R.id.sr_list_item_online)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.sr_list_item_online)).setVisibility(View.GONE);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("SearchActivity")) {
                    ((SearchActivity) ctx).hideSelectedItemBackground(position);
                    ((SearchActivity) ctx).showProfile(position);
                }
            }
        });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */

        view.setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        });

        return view;
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }

}

