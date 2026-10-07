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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import dev.tinelix.twemojicon.emoji.Emojicon;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 * @author Dmitry Tretyakov (tinelix@mail.ru)
 */
class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.Holder> {
    private Fragment mFragment;
    private boolean mUseSystemDefault = false;
    private ArrayList<Emojicon> mEmojiData;
    private Context mContext;

    public EmojiAdapter(Context context, ArrayList<Emojicon> data) {
        mUseSystemDefault = false;
        this.mEmojiData = data;
        this.mContext = context;
    }

    public EmojiAdapter(Context context, Fragment fragment, ArrayList<Emojicon> data, boolean useSystemDefault) {
        mUseSystemDefault = useSystemDefault;
        this.mEmojiData = data;
        this.mContext = context;
        this.mFragment = fragment;
    }

    public EmojiAdapter(Context context, Fragment fragment, Emojicon[] data, boolean mUseSystemDefault) {
        this.mUseSystemDefault = mUseSystemDefault;
        this.mEmojiData = new ArrayList<>(Arrays.asList(data));
        this.mContext = context;
        this.mFragment = fragment;
    }

    public EmojiAdapter(Context context, Emojicon[] data, boolean useSystemDefault) {
        mUseSystemDefault = useSystemDefault;
        this.mEmojiData = new ArrayList<>(Arrays.asList(data));
        this.mContext = context;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(
                LayoutInflater.from(mContext).inflate(R.layout.emojicon_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    public Emojicon getEmojicon(int position) {
        return mEmojiData.get(position);
    }

    @Override
    public int getItemCount() {
        return mEmojiData.size();
    }

    public class Holder extends RecyclerView.ViewHolder {
        //private final View view;
        EmojiconTextView iconView;

        public Holder(View itemView) {
            super(itemView);
            //view = itemView;
            iconView = itemView.findViewById(R.id.emojicon_icon);
        }

        public void bind(final int position) {
            Emojicon icon = getEmojicon(position);
            iconView.setUseSystemDefault(mUseSystemDefault);
            iconView.setText(icon.getEmoji());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mFragment != null) {
                        if (mFragment instanceof EmojiconGridFragment)
                            ((EmojiconGridFragment) mFragment).clickEmojiItem(position);
                        else if(mFragment instanceof EmojiconRecentsGridFragment)
                            ((EmojiconRecentsGridFragment) mFragment).clickEmojiItem(position);
                    }
                }
            });
        }
    }
}
