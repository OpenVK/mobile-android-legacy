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
import uk.openvk.android.legacy.ui.core.activities.FriendsIntentActivity;
import uk.openvk.android.legacy.api.models.Friend;
import uk.openvk.android.legacy.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.legacy.ui.text.CenteredImageSpan;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class FriendsListAdapter extends BaseAdapter {
    private final FriendsFragment friendsFragment;
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Friend> objects;
    public boolean opened_sliding_menu;

    public FriendsListAdapter(Context context, FriendsFragment friendsFragment, ArrayList<Friend> items) {
        this.friendsFragment = friendsFragment;
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

    Friend getFriend(int position) {
        return ((Friend) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_friend, parent, false);
        }

        final Friend item = getFriend(position);
        if(item.verified) {
            String name = String.format("%s %s  ", item.first_name, item.last_name);
            SpannableStringBuilder sb = new SpannableStringBuilder(name);
            ImageSpan imageSpan;
            imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black);
            ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 * ctx.getResources().getDisplayMetrics().density));
            sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((TextView) view.findViewById(R.id.flist_item_text)).setText(sb);
        } else {
            ((TextView) view.findViewById(R.id.flist_item_text)).setText(String.format("%s %s", item.first_name, item.last_name));
        }
        if(item.online) {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.VISIBLE);
        } else {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setVisibility(View.GONE);
        }
        if(item.avatar != null) {
            ((ImageView) view.findViewById(R.id.flist_item_photo)).setImageBitmap(item.avatar);
        } else {
            ((ImageView) view.findViewById(R.id.flist_item_photo)).setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
        }

        if(item.from_mobile) {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_online_mobile));
        } else {
            ((ImageView) view.findViewById(R.id.flist_item_online)).setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_online));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    friendsFragment.hideSelectedItemBackground(position);
                    ((AppActivity) ctx).showProfile(item.id);
                } else if(ctx.getClass().getSimpleName().equals("FriendsIntentActivity")) {
                    friendsFragment.hideSelectedItemBackground(position);
                    ((FriendsIntentActivity) ctx).showProfile(item.id);
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

