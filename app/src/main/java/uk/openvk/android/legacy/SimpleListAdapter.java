package uk.openvk.android.legacy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SimpleListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<SimpleListItem> objects;
    SimpleListAdapter(Context context, ArrayList<SimpleListItem> items) {
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

    SimpleListItem getListItem(int position) {
        return ((SimpleListItem) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.simple_list_item, parent, false);
            SimpleListItem item = getListItem(position);
            TextView item_name = view.findViewById(R.id.item_title);
            item_name.setText(item.name);
        }

        SimpleListItem item = getListItem(position);
        ((TextView) view.findViewById(R.id.item_title)).setText(item.name);
        ((TextView) view.findViewById(R.id.item_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((AppActivity) ctx).onSimpleListItemClicked(position);
            }
        });
        return view;
    }

    public class ViewHolder {
        public TextView item_name;
    }


}

