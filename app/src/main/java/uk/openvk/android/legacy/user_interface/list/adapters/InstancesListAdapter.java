package uk.openvk.android.legacy.user_interface.list.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.core.activities.AuthActivity;
import uk.openvk.android.legacy.user_interface.list.items.InstancesListItem;

public class InstancesListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<InstancesListItem> objects;
    public InstancesListAdapter(Context context, ArrayList<InstancesListItem> items) {
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

    InstancesListItem getListItem(int position) {
        return ((InstancesListItem) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.instance_item, parent, false);
            InstancesListItem item = getListItem(position);
            TextView item_name = (TextView) view.findViewById(R.id.item_title);
            item_name.setText(item.server);
            ImageView item_official = (ImageView) view.findViewById(R.id.official_state);
            if(item.official) {
                item_official.setVisibility(View.VISIBLE);
            } else {
                item_official.setVisibility(View.GONE);
            }
        }

        InstancesListItem item = getListItem(position);
        ((TextView) view.findViewById(R.id.item_title)).setText(item.server);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    //((AppActivity) ctx).onSimpleListItemClicked(position);
                } else if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
                    ((AuthActivity) ctx).clickInstancesItem(position);
                }
            }
        });
        ((ImageView) view.findViewById(R.id.official_state)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    //((AppActivity) ctx).onSimpleListItemClicked(position);
                } else if(ctx.getClass().getSimpleName().equals("AuthActivity")) {
                    ((AuthActivity) ctx).clickInstancesItem(position);
                }
            }
        });
        ((ImageView) view.findViewById(R.id.official_state)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(ctx, ctx.getResources().getText(R.string.official_state), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        return view;
    }

    public class ViewHolder {
        public TextView item_name;
    }


}

