/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.core.activities.AuthActivity;
import uk.openvk.android.legacy.ui.list.items.InstancesListItem;
import uk.openvk.android.legacy.ui.views.AutoCompleteEditText;

public class InstancesListAdapter extends ArrayAdapter<InstancesListItem> {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<InstancesListItem> objects;
    ArrayList<InstancesListItem> filteredObjects;

    public InstancesListAdapter(Context context, ArrayList<InstancesListItem> items) {
        super(context, R.layout.list_item_instance, items);
        ctx = context;
        objects = items;
        filteredObjects = new ArrayList<>();
        filteredObjects.addAll(objects);
    }

    public ArrayList<InstancesListItem> getFilteredInstances() {
        return filteredObjects;
    }

    @Override
    public int getCount() {
        return filteredObjects.size();
    }

    @Override
    public InstancesListItem getItem(int position) {
        return filteredObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    InstancesListItem getListItem(int position) {
        return getItem(position);
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(final int position, View view, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(inflater != null)
            view = inflater.inflate(R.layout.list_item_instance, null, true);

        if (view != null) {
            InstancesListItem item = getListItem(position);
            TextView item_name = view.findViewById(R.id.item_title);
            item_name.setText(item.server);
            ImageView item_official = view.findViewById(R.id.official_state);
            TextView https_chip = view.findViewById(R.id.https_chip);
            item_official.setVisibility(item.official ? View.VISIBLE : View.GONE);
            https_chip.setVisibility(item.secured ? View.VISIBLE : View.GONE);

            if (item.restricted) {
                TextView note_text = view.findViewById(R.id.note_text);
                note_text.setText(ctx.getResources().getString(R.string.maybe_restricted_in_your_country));
                note_text.setVisibility(View.VISIBLE);
            }

            ((TextView) view.findViewById(R.id.item_title)).setText(item.server);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx instanceof AuthActivity) {
                        ((AuthActivity) ctx).clickInstancesItem(position);
                    }
                }
            });
            view.findViewById(R.id.official_state).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ctx instanceof AuthActivity) {
                        ((AuthActivity) ctx).clickInstancesItem(position);
                    }
                }
            });
            view.findViewById(R.id.official_state).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(ctx, ctx.getResources().getText(R.string.official_state), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }
        return view;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<InstancesListItem> matchingItems = new ArrayList<>();

                if(constraint == null || constraint.length() == 0) {
                    results.values = objects;
                    results.count = objects.size();

                    AutoCompleteEditText tv = null;

                    if(ctx instanceof AuthActivity)
                        tv = ((AuthActivity) ctx).findViewById(R.id.instance_name);

                    if(tv != null)
                        tv.animateDropDownState(false);

                } else {
                    String pattern = constraint.toString().toLowerCase().trim();

                    for (InstancesListItem instance : objects) {
                        if(instance.server.startsWith(pattern))
                            matchingItems.add(instance);
                    }

                    results.values = matchingItems;
                    results.count = matchingItems.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if(filterResults.values != null) {
                    ArrayList<?> filteredObjects = (ArrayList<?>) filterResults.values;

                    InstancesListAdapter.this.filteredObjects.clear();

                    for (Object obj : filteredObjects) {
                        InstancesListAdapter.this.filteredObjects.add((InstancesListItem) obj);
                    }

                    AutoCompleteEditText tv = null;

                    if(ctx instanceof AuthActivity)
                        tv = ((AuthActivity) ctx).findViewById(R.id.instance_name);

                    if (filterResults.count > 0) {
                        notifyDataSetChanged();
                        if(tv != null)
                            tv.animateDropDownState(true);
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return resultValue.toString();
            }
        };
    }
}

