package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.QuickSearchActivity;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.api.models.User;

public class GroupsSearchResultAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Group> objects;

    public GroupsSearchResultAdapter(Context context, ArrayList<Group> items) {
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
    public Group getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    Group getGroup(int position) {
        return ((Group) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.search_result_item, parent, false);
        }

        Group item = getGroup(position);
        ((TextView) view.findViewById(R.id.sr_list_item_text)).setText(item.name);
        ((TextView) view.findViewById(R.id.sr_list_item_subtext)).setVisibility(View.GONE);
        ((ImageView) view.findViewById(R.id.sr_list_item_online)).setVisibility(View.GONE);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("QuickSearchActivity")) {
                    ((QuickSearchActivity) ctx).hideSelectedItemBackground(position);
                    ((QuickSearchActivity) ctx).showGroup(position);
                }
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

