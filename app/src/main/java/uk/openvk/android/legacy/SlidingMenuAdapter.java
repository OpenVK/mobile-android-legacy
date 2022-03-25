package uk.openvk.android.legacy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SlidingMenuAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<SlidingMenuItem> objects;
    public boolean opened_sliding_menu;
    SlidingMenuAdapter(Context context, ArrayList<SlidingMenuItem> items) {
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

    SlidingMenuItem getSlidingMenuItem(int position) {
        return ((SlidingMenuItem) getItem(position));
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.left_menu_item, parent, false);
        }

        final int position = i;

        SlidingMenuItem item = getSlidingMenuItem(i);
        ((TextView) view.findViewById(R.id.leftmenu_text)).setText(item.name);
        if(item.counter == 0) {
            ((TextView) view.findViewById(R.id.leftmenu_counter)).setVisibility(View.GONE);
        } else {
            ((TextView) view.findViewById(R.id.leftmenu_counter)).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.leftmenu_counter)).setText("" + item.counter);
        }
        ((ImageView) view.findViewById(R.id.leftmenu_icon)).setImageDrawable(item.icon);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AppActivity) ctx).onSlidingMenuItemClicked(position);
            }
        });
        return view;
    }
}
