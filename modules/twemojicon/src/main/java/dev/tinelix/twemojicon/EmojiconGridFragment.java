/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.tinelix.twemojicon;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import dev.tinelix.twemojicon.emoji.Emojicon;
import dev.tinelix.twemojicon.emoji.People;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
public class EmojiconGridFragment extends Fragment {
    private OnEmojiconClickedListener mOnEmojiconClickedListener;
    private EmojiconRecents mRecents;
    private Emojicon[] mData;
    private boolean mUseSystemDefault = false;

    private static final String USE_SYSTEM_DEFAULT_KEY = "useSystemDefaults";
    private RecyclerView mGridView;
    private EmojiAdapter mEmojiAdapter;

    protected static EmojiconGridFragment newInstance(Emojicon[] emojicons, EmojiconRecents recents) {
        return newInstance(emojicons, recents, false);
    }

    protected static EmojiconGridFragment newInstance(Emojicon[] emojicons, EmojiconRecents recents, boolean useSystemDefault) {
        EmojiconGridFragment emojiGridFragment = new EmojiconGridFragment();
        Bundle args = new Bundle();
        args.putSerializable("emojicons", emojicons);
        args.putBoolean(USE_SYSTEM_DEFAULT_KEY, useSystemDefault);
        emojiGridFragment.setArguments(args);
        emojiGridFragment.setRecents(recents);
        return emojiGridFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.emojicon_grid, container, false);
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mGridView = view.findViewById(R.id.emoji_grid);

        Bundle bundle = getArguments();
        if (bundle == null) {
            mData = People.DATA;
            mUseSystemDefault = false;
        } else {
            Object[] o = (Object[]) getArguments().getSerializable("emojicons");
            if(o != null)
                mData = Arrays.asList(o).toArray(new Emojicon[o.length]);
            mUseSystemDefault = bundle.getBoolean(USE_SYSTEM_DEFAULT_KEY);
        }

        mEmojiAdapter = new EmojiAdapter(view.getContext(), this, mData, mUseSystemDefault);

        mGridView.setHasFixedSize(true);
        mGridView.setLayoutManager(new GridLayoutManager(getContext(), 10));
        mGridView.setItemViewCacheSize(200);
        mGridView.setAdapter(mEmojiAdapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("emojicons", mData);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnEmojiconClickedListener) {
            mOnEmojiconClickedListener = (OnEmojiconClickedListener) activity;
        } else if (getParentFragment() instanceof OnEmojiconClickedListener) {
            mOnEmojiconClickedListener = (OnEmojiconClickedListener) getParentFragment();
        } else {
            throw new IllegalArgumentException(activity + " must implement interface " + OnEmojiconClickedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        mOnEmojiconClickedListener = null;
        super.onDetach();
    }

    public void clickEmojiItem(int position) {

        Emojicon emojicon = mEmojiAdapter.getEmojicon(position);

        if (mOnEmojiconClickedListener != null) {
            mOnEmojiconClickedListener.onEmojiconClicked(emojicon);
        }
        if (mRecents != null) {
            mRecents.addRecentEmoji(getContext(), emojicon);
        }
    }

    private void setRecents(EmojiconRecents recents) {
        mRecents = recents;
    }

    public interface OnEmojiconClickedListener {
        void onEmojiconClicked(Emojicon emojicon);
    }
}
