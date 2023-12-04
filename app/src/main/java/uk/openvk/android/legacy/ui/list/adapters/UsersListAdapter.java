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
import uk.openvk.android.legacy.api.entities.User;
import uk.openvk.android.legacy.ui.core.activities.base.UsersListActivity;
import uk.openvk.android.legacy.ui.core.fragments.base.UsersFragment;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class UsersListAdapter extends BaseAdapter {
    private final UsersFragment usersFragment;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<User> objects;
    public boolean opened_sliding_menu;

    public UsersListAdapter(Context context, UsersFragment usersFragment, ArrayList<User> items) {
        this.usersFragment = usersFragment;
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
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    User getUser(int position) {
        return ((User) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_user, parent, false);
        }

        final User item = getUser(position);
        if(item.verified) {
            String name = String.format("%s %s  ", item.first_name, item.last_name);
            SpannableStringBuilder sb = new SpannableStringBuilder(name);
            ImageSpan imageSpan;
            imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black);
            ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 *
                    ctx.getResources().getDisplayMetrics().density));
            sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) view.findViewById(R.id.ulist_item_text)).setText(sb);
        } else {
            ((TextView) view.findViewById(R.id.ulist_item_text)).setText(
                    String.format("%s %s", item.first_name, item.last_name));
        }
        if(item.online) {
            ((ImageView) view.findViewById(R.id.ulist_item_online)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.ulist_item_online)).setVisibility(View.GONE);
        }
        if(item.avatar != null) {
            ((ImageView) view.findViewById(R.id.ulist_item_photo)).setImageBitmap(item.avatar);
        } else {
            ((ImageView) view.findViewById(R.id.ulist_item_photo)).setImageDrawable(
                    ctx.getResources().getDrawable(R.drawable.photo_loading));
        }

//        if(item.from_mobile) {
//            ((ImageView) view.findViewById(R.id.flist_item_online)).setImageDrawable(
//                    ctx.getResources().getDrawable(R.drawable.ic_online_mobile));
//        } else {
//            ((ImageView) view.findViewById(R.id.flist_item_online)).setImageDrawable(
//                    ctx.getResources().getDrawable(R.drawable.ic_online));
//        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx instanceof UsersListActivity) {
                    ((UsersListActivity) ctx).showProfile(item.id);
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

