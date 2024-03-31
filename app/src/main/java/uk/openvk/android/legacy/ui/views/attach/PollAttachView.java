/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
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

package uk.openvk.android.legacy.ui.views.attach;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.client.entities.Poll;
import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.legacy.ui.list.adapters.PollAdapter;

public class PollAttachView extends LinearLayout {

    private PollAdapter pollAdapter;
    private LinearLayoutManager llm;

    public PollAttachView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_poll, null);

        this.addView(view);
        view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public PollAttachView(Context ctx) {
        super(ctx);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_poll, null);

        this.addView(view);
        view.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
    }

    public void createAdapter(Context ctx, int item_pos, ArrayList<WallPost> wallPosts,
                              WallPost post,
                              ArrayList<Poll.PollAnswer> answers,
                              boolean multiple, int user_votes, long total_votes) {
        pollAdapter = new PollAdapter(ctx, item_pos,
                wallPosts, post, answers, multiple, user_votes, total_votes);
        llm = new LinearLayoutManager(ctx);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        ((RecyclerView) findViewById(R.id.answer_list)).setLayoutManager(llm);
        ((RecyclerView) findViewById(R.id.answer_list)).setAdapter(pollAdapter);
    }

    public void setPollInfo(String question, boolean anonymous, long end_date) {
        ((TextView) findViewById(R.id.question_name)).setText(question);
        if(anonymous) {
            ((TextView) findViewById(R.id.poll_info)).setText(getResources().getString(R.string.poll_anonym));
        } else {
            ((TextView) findViewById(R.id.poll_info)).setText(getResources().getString(R.string.poll_open));
        }
    }
}
