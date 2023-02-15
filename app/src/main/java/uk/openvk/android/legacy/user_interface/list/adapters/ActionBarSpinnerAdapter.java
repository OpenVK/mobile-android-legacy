package uk.openvk.android.legacy.user_interface.list.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.core.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.list.items.SimpleListItem;

@SuppressWarnings("ResourceType")
public class ActionBarSpinnerAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<SimpleListItem> objects;
    public int textColor;
    public int selectedTextColor;
    public String from;
    public ActionBarSpinnerAdapter(Context context, ArrayList<SimpleListItem> items, int _color, int _selectedColor, String from) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        textColor = _color;
        selectedTextColor = _selectedColor;
        this.from = from;
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
            view = inflater.inflate(R.layout.actionbar_spinner_item, parent, false);
            SimpleListItem item = getListItem(position);
            TextView item_name = view.findViewById(R.id.item_title);
            item_name.setText(item.name);
        }
        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
            Global global = new Global(ctx);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                if(global.isTablet()) {
                    TextView item_name = view.findViewById(R.id.item_title);
                    LinearLayout.LayoutParams ll_layoutParams = (LinearLayout.LayoutParams) item_name.getLayoutParams();
                    ll_layoutParams.setMargins(0, ((int)(-2 * ctx.getResources().getDisplayMetrics().density)), 0, 0);
                    item_name.setLayoutParams(ll_layoutParams);
                    TextView app_title = view.findViewById(R.id.app_title);
                    ll_layoutParams = (LinearLayout.LayoutParams) app_title.getLayoutParams();
                    ll_layoutParams.setMargins(0, ((int)(-4 * ctx.getResources().getDisplayMetrics().density)), 0, 0);
                    item_name.setLayoutParams(ll_layoutParams);
                } else if (ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    TextView item_name = view.findViewById(R.id.item_title);
                    LinearLayout.LayoutParams ll_layoutParams = (LinearLayout.LayoutParams) item_name.getLayoutParams();
                    ll_layoutParams.setMargins(0, ((int)(-2 * ctx.getResources().getDisplayMetrics().density)), 0, 0);
                    item_name.setLayoutParams(ll_layoutParams);
                    TextView app_title = view.findViewById(R.id.app_title);
                    ll_layoutParams = (LinearLayout.LayoutParams) app_title.getLayoutParams();
                    ll_layoutParams.setMargins(0, ((int)(-4 * ctx.getResources().getDisplayMetrics().density)), 0, 0);
                    item_name.setLayoutParams(ll_layoutParams);
                }
            } else {
                TextView item_name = view.findViewById(R.id.item_title);
                LinearLayout.LayoutParams ll_layoutParams = (LinearLayout.LayoutParams) item_name.getLayoutParams();
                ll_layoutParams.setMargins(0, ((int)(-2 * ctx.getResources().getDisplayMetrics().density)), 0, 0);
                item_name.setLayoutParams(ll_layoutParams);
                TextView app_title = view.findViewById(R.id.app_title);
                ll_layoutParams = (LinearLayout.LayoutParams) app_title.getLayoutParams();
                ll_layoutParams.setMargins(0, ((int)(-2 * ctx.getResources().getDisplayMetrics().density)), 0, 0);
                item_name.setLayoutParams(ll_layoutParams);
            }
        }

        SimpleListItem item = getListItem(position);
        ((TextView) view.findViewById(R.id.item_title)).setText(item.name);
        ((TextView) view.findViewById(R.id.item_title)).setSingleLine(true);
        ((TextView) view.findViewById(R.id.item_title)).setTextColor(selectedTextColor);
        ((TextView) view.findViewById(R.id.app_title)).setTextColor(selectedTextColor);
        if(from.equals("newsfeed_actionbar")) {
            ((TextView) view.findViewById(R.id.app_title)).setText(ctx.getResources().getString(R.string.newsfeed));
        }
        return view;
    }

    @Override
    public View getDropDownView(final int position, View convertView, ViewGroup parent) {
        View view;
        view = convertView;
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) ctx.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.simple_list_item, null);// layout for spinner list
        }
        SimpleListItem item = getListItem(position);
        ((TextView) view.findViewById(R.id.item_title)).setText(item.name);
        ((TextView) view.findViewById(R.id.item_title)).setTextColor(textColor);
        ((TextView) view.findViewById(R.id.item_title)).setSingleLine(true);
        ((TextView) view.findViewById(R.id.item_title)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(from.equals("newsfeed_actionbar")) {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        ((TextView) view.findViewById(R.id.item_title)).setTextColor(selectedTextColor);
                    }
                    ((AppActivity) ctx).selectNewsSpinnerItem(position);
                }
            }
        });

        return view;
    }

    public class ViewHolder {
        public TextView item_name;
    }


}

