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

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.Poll;
import uk.openvk.android.client.entities.WallPost;
import uk.openvk.android.legacy.core.activities.base.NetworkActivity;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.Holder> {

    private final ArrayList<Poll.PollAnswer> answers;
    private Poll poll;
    private Context ctx;
    private int total_votes;

    public PollAdapter(Context context, Poll poll) {
        ctx = context;
        this.poll = poll;
        this.answers = poll.answers;
    }

    @Override
    public PollAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PollAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.layout_poll_answer,
                parent, false));
    }

    @Override
    public void onBindViewHolder(PollAdapter.Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(PollAdapter.Holder holder) {
        super.onViewRecycled(holder);
    }

    public Poll.PollAnswer getItem(int position) {
        return answers.get(position);
    }

    @Override
    public int getItemCount() {
        return answers.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final View convertView;
        public final TextView answer_name;
        public final ProgressBar answer_progress;
        public final TextView answer_progress_value;
        public final TextView answer_votes_count;
        private final LinearLayout answer_votes_layout;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.answer_votes_layout = convertView.findViewById(R.id.answer_votes);
            this.answer_name = convertView.findViewById(R.id.answer_name);
            this.answer_progress = convertView.findViewById(R.id.answer_progress);
            this.answer_progress_value = convertView.findViewById(R.id.answer_progress_value);
            this.answer_votes_count = convertView.findViewById(R.id.answer_votes_count);
        }

        void bind(final int position) {
            final Poll.PollAnswer item = getItem(position);
            answer_name.setText(item.text);
            int item_votes;
            if(poll.user_votes > 0) {
                total_votes = item.votes + 1;
                if(item.is_voted) {
                    answer_name.setTypeface(Typeface.DEFAULT_BOLD);
                    answer_progress.setProgressDrawable(ctx.getResources().getDrawable(
                            R.drawable.horizontal_progress));
                    answer_votes_count.setTextColor(ctx.getResources().getColor(R.color.ovk_color));
                    item_votes = item.votes + 1;
                    answer_votes_count.setText(String.valueOf(item_votes));
                } else {
                    item_votes = item.votes;
                    answer_name.setTypeface(Typeface.DEFAULT);
                    answer_progress.setProgressDrawable(ctx.getResources().
                            getDrawable(R.drawable.horizontal_progress_2));
                    answer_votes_count.setTextColor(Color.parseColor("#6f6f6f"));
                    answer_votes_count.setText(String.valueOf(item_votes));
                }
                answer_progress.setMax(total_votes);
                answer_progress.setProgress(item_votes);
                double progress = (double) item_votes / (double) total_votes;
                answer_progress_value.setText(String.format("%s%%", (int)(progress * 100)));
                answer_progress.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        removeVoteInPoll();
                        return true;
                    }
                });
            } else if(poll.user_votes == 0) {
                total_votes = item.votes;
                answer_name.setTypeface(Typeface.DEFAULT);
                answer_progress.setMax(total_votes);
                answer_progress.setProgress(0);
                answer_votes_count.setText(ctx.getResources().getString(R.string.poll_btn_vote));
                answer_progress_value.setVisibility(View.GONE);
                answer_votes_count.setTextColor(Color.parseColor("#6f6f6f"));
                answer_progress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        voteInPoll(position);
                    }
                });
            }
        }

        public void voteInPoll(int answer) {
            try {
                poll.user_votes = 1;
                if (!poll.answers.get(answer).is_voted) {
                    poll.answers.get(answer).is_voted = true;
                }
                poll.vote(((NetworkActivity) ctx).ovk_api.wrapper, poll.answers.get(answer).id);
                notifyItemChanged(answer);
            } catch (Exception ex) {
                Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }

        public void removeVoteInPoll() {
            try {
                poll.user_votes = 0;
                for (int i = 0; i < poll.answers.size(); i++) {
                    if (poll.answers.get(i).is_voted) {
                        poll.answers.get(i).is_voted = false;
                    }
                }
                poll.unvote(((NetworkActivity) ctx).ovk_api.wrapper);
                notifyDataSetChanged();
            } catch (Exception ex) {
                Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
