package uk.openvk.android.legacy.core.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import uk.openvk.android.legacy.OvkApplication;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Account;
import uk.openvk.android.legacy.api.entities.Audio;
import uk.openvk.android.legacy.api.entities.Note;
import uk.openvk.android.legacy.ui.list.adapters.NotesListAdapter;
import uk.openvk.android.legacy.ui.utils.WrappedGridLayoutManager;
import uk.openvk.android.legacy.ui.utils.WrappedLinearLayoutManager;

public class AudiosFragment extends Fragment {
    private RecyclerView audiosView;
    private Account account;
    private View view;
    private String instance;
    private ArrayList<Audio> audios;

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
            audiosAdapter = new AudiosListAdapter(ctx, audios);
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
            notesAdapter.notifyDataSetChanged();
        }
    }
}
