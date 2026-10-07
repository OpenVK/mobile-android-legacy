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

import dev.tinelix.twemojicon.emoji.Emojicon;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.GridView;

/**
 * @author Daniele Ricci
 */
public class EmojiconRecentsGridFragment extends EmojiconGridFragment implements EmojiconRecents {
    private EmojiAdapter mAdapter;
    private boolean mUseSystemDefault = false;

    private static final String USE_SYSTEM_DEFAULT_KEY = "useSystemDefaults";

    protected static EmojiconRecentsGridFragment newInstance() {
        return newInstance(false);
    }

    protected static EmojiconRecentsGridFragment newInstance(boolean useSystemDefault) {
        EmojiconRecentsGridFragment fragment = new EmojiconRecentsGridFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(USE_SYSTEM_DEFAULT_KEY, useSystemDefault);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUseSystemDefault = getArguments().getBoolean(USE_SYSTEM_DEFAULT_KEY);
        } else {
            mUseSystemDefault = false;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        EmojiconRecentsManager recents = EmojiconRecentsManager
            .getInstance(view.getContext());

        mAdapter = new EmojiAdapter(view.getContext(), this, recents, mUseSystemDefault);
        RecyclerView gridView = view.findViewById(R.id.emoji_grid);
        gridView.setHasFixedSize(true);
        gridView.setLayoutManager(new GridLayoutManager(getContext(), 10));
        gridView.setItemViewCacheSize(200);
        gridView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mAdapter = null;
    }

    @Override
    public void addRecentEmoji(Context context, Emojicon emojicon) {
        EmojiconRecentsManager recents = EmojiconRecentsManager
            .getInstance(context);
        recents.push(emojicon);

        // notify dataset changed
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

}
