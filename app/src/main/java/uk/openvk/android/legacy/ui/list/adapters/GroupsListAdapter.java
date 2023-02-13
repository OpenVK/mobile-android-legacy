package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.GroupIntentActivity;
import uk.openvk.android.legacy.api.models.Group;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

public class GroupsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Group> objects;
    public boolean opened_sliding_menu;

    public GroupsListAdapter(Context context, ArrayList<Group> items) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
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
        return (getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.group_list_item, parent, false);
        }

        Group item = getItem(position);
        if(item.verified) {
            String name = String.format("%s  ", item.name);
            SpannableStringBuilder sb = new SpannableStringBuilder(name);
            ImageSpan imageSpan;
            imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black);
            ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 * ctx.getResources().getDisplayMetrics().density));
            sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) view.findViewById(R.id.group_list_item_text)).setText(sb);
        } else {
            ((TextView) view.findViewById(R.id.group_list_item_text)).setText(String.format("%s", item.name));
        }

        if(item.members_count > 0) {
            ((TextView) view.findViewById(R.id.group_list_item_subtext)).setText(String.format("%s %s", item.members_count, ctx.getResources().getStringArray(R.array.profile_followers)[2]));
        }

        if(item.avatar != null) {
            ((ImageView) view.findViewById(R.id.group_list_item_photo)).setImageBitmap(item.avatar);
        } else {
            ((ImageView) view.findViewById(R.id.group_list_item_photo)).setImageDrawable(ctx.getResources().getDrawable(R.drawable.group_placeholder));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).hideSelectedItemBackground(position);
                    ((AppActivity) ctx).showGroup(position);
                } else if(ctx.getClass().getSimpleName().equals("GroupsIntentActivity")) {
                    ((GroupIntentActivity) ctx).hideSelectedItemBackground(position);
                    ((GroupIntentActivity) ctx).showGroup(position);
                }
            }
        });

        /* ((TextView) view.findViewById(R.id.post_view)).setOnTouchListener(new SwipeListener(ctx) {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return super.onTouch(v, event);
            }
        }); */

        return view;
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }

}

