package uk.openvk.android.legacy.core.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Note;
import uk.openvk.android.legacy.core.fragments.base.ActiviableFragment;
import uk.openvk.android.legacy.ui.list.adapters.NotesListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;

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

public class NotesFragment extends ActiviableFragment {
    public TextView titlebar_title;
    public String state;
    public String send_request;
    public SharedPreferences global_prefs;
    private RecyclerView notesListView;
    private ArrayList<Note> notes;
    private NotesListAdapter notesAdapter;
    private boolean loading_more_notes = false;
    private View view;
    private Context activity_ctx;
    private String instance;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notes, container, false);
        notesListView = view.findViewById(R.id.notes_listview);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Note> notes) {
        this.notes = notes;
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        if (notesAdapter == null) {
            notesAdapter = new NotesListAdapter(ctx, notes);
            if(app.isTablet && app.swdp >= 760) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 3);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.notes_listview)).setLayoutManager(glm);
            } else if(app.isTablet && app.swdp >= 600) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.notes_listview)).setLayoutManager(glm);
            } else {
                LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                ((RecyclerView) view.findViewById(R.id.notes_listview)).setLayoutManager(llm);
            }
            notesListView.setAdapter(notesAdapter);
        } else {
            notesAdapter.notifyDataSetChanged();
        }
    }

    public int getCount() {
        try {
            return notesAdapter.getItemCount();
        } catch(Exception ex) {
            return 0;
        }
    }

    public void setScrollingPositions(final Context ctx, final boolean infinity_scroll) {
        loading_more_notes = false;
        // TODO: Add infinity scroll for RecyclerView (must be inside InfinityNestedScrollView / InfinityScrollView)
        /* if(infinity_scroll) {
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount) {
                        if(!loading_more_friends) {
                            if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                                loading_more_friends = true;
                                ((AppActivity) ctx).loadMoreFriends();
                            }
                        }
                    }
                }
        */
    }

    public void setActivityContext(Context ctx) {
        activity_ctx = ctx;
    }
}
