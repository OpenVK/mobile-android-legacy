package uk.openvk.android.legacy.core.fragments;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.list.adapters.AudiosListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;

/*  Copyleft © 2022, 2023 OpenVK Team
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
 */

public class AudiosFragment extends Fragment {
    private RecyclerView audiosView;
    private Account account;
    private View view;
    private String instance;
    private ArrayList<Audio> audios;
    private AudiosListAdapter audiosAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_audios, container, false);
        audiosView = view.findViewById(R.id.audios_listview);
        instance = ((OvkApplication) getContext().getApplicationContext()).getCurrentInstance();
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Audio> audios) {
        this.audios = audios;
        OvkApplication app = ((OvkApplication)getContext().getApplicationContext());
        if (audiosAdapter == null) {
            LinearLayout bottom_player_view = view.findViewById(R.id.audio_player_bar);
            audiosAdapter = new AudiosListAdapter(ctx, bottom_player_view, new MediaPlayer(), audios);
            if(app.isTablet && app.swdp >= 760) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 3);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                audiosView.setLayoutManager(glm);
            } else if(app.isTablet && app.swdp >= 600) {
                LinearLayoutManager glm = new WrappedGridLayoutManager(ctx, 2);
                glm.setOrientation(LinearLayoutManager.VERTICAL);
                audiosView.setLayoutManager(glm);
            } else {
                LinearLayoutManager llm = new WrappedLinearLayoutManager(ctx);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                audiosView.setLayoutManager(llm);
            }
            audiosView.setAdapter(audiosAdapter);
        } else {
            audiosAdapter.notifyDataSetChanged();
        }
    }

    public void setScrollingPositions(Context ctx, boolean b) {
    }
}
