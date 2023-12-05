package uk.openvk.android.legacy.ui.list.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.api.entities.Group;
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

public class GroupsListAdapter extends RecyclerView.Adapter<GroupsListAdapter.Holder> {
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

    public Group getItem(int position) {
        return objects.get(position);
    }

    @Override
    public GroupsListAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GroupsListAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_group, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
    }

    Group getGroup(int position) {
        return (getItem(position));
    }

    public class Holder extends RecyclerView.ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_subtext;
        public ImageView item_avatar;
        public View view;
        public Holder(View convertView) {
            super(convertView);
            view = convertView;
            item_name = (view.findViewById(R.id.group_list_item_text));
            item_subtext = (view.findViewById(R.id.group_list_item_subtext));
            item_avatar = (view.findViewById(R.id.group_list_item_photo));
        }

        void bind(final int position) {
            Group item = getItem(position);
            if(item.verified) {
                String name = String.format("%s  ", item.name);
                SpannableStringBuilder sb = new SpannableStringBuilder(name);
                ImageSpan imageSpan;
                imageSpan = new CenteredImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black);
                ((CenteredImageSpan) imageSpan).getDrawable().setBounds(0, 0, 0, (int)(6 *
                        ctx.getResources().getDisplayMetrics().density));
                sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ((TextView) view.findViewById(R.id.group_list_item_text)).setText(sb);
            } else {
                ((TextView) view.findViewById(R.id.group_list_item_text)).setText(
                        String.format("%s", item.name));
            }

            if(item.members_count > 0) {
                ((TextView) view.findViewById(R.id.group_list_item_subtext)).setText(
                        String.format("%s %s", item.members_count,
                                Global.getPluralQuantityString(ctx.getApplicationContext(),
                                        R.plurals.profile_followers, 0)));
            }

            if(item.avatar != null) {
                ((ImageView) view.findViewById(R.id.group_list_item_photo)).setImageBitmap(
                        item.avatar);
            } else {
                ((ImageView) view.findViewById(R.id.group_list_item_photo)).setImageDrawable(
                        ctx.getResources().getDrawable(R.drawable.group_placeholder));
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).hideSelectedItemBackground();
                        showGroup(position);
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
        }

        public void showGroup(int position) {
            String url = "openvk://group/" + "club" + getItem(position).id;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.setPackage("uk.openvk.android.legacy");
            ctx.startActivity(i);
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

}

