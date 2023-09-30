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
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.base.NetworkActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.api.entities.PollAnswer;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.Holder> {

    private final ArrayList<WallPost> wallPosts;
    private WallPost post;
    private int item_pos;
    private long total_votes;
    private ArrayList<PollAnswer> items = new ArrayList<>();
    private Context ctx;
    private boolean multiple;
    public LruCache memCache;
    private int user_votes;
    private long total_votes_2;

    public PollAdapter(Context context, int item_pos, ArrayList<WallPost> wallPosts, WallPost post,
                       ArrayList<PollAnswer> answers, boolean multiple,
                       int user_votes, long total_votes) {
        ctx = context;
        this.item_pos = item_pos;
        this.post = post;
        items = answers;
        this.wallPosts = wallPosts;
        this.multiple = multiple;
        this.user_votes = user_votes;
        this.total_votes = total_votes;
        this.total_votes_2 = total_votes;
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

    public PollAnswer getItem(int position) {
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
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
            this.answer_votes_layout = (LinearLayout) convertView.findViewById(R.id.answer_votes);
            this.answer_name = (TextView) convertView.findViewById(R.id.answer_name);
            this.answer_progress = (ProgressBar) convertView.findViewById(R.id.answer_progress);
            this.answer_progress_value = (TextView) convertView.findViewById(R.id.answer_progress_value);
            this.answer_votes_count = (TextView) convertView.findViewById(R.id.answer_votes_count);
        }

        void bind(final int position) {
            final PollAnswer item = getItem(position);
            answer_name.setText(item.text);
            int item_votes = item.votes;
            if(user_votes > 0) {
                total_votes = total_votes_2 + 1;
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
                answer_progress.setMax((int) total_votes);
                answer_progress.setProgress(item_votes);
                double progress = (double) item_votes / (double) total_votes;
                answer_progress_value.setText(String.format("%s%%", (int)(progress * 100)));
                answer_progress.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        removeVoteInPoll(item_pos, post);
                        return true;
                    }
                });
            } else if(user_votes == 0) {
                total_votes = total_votes_2;
                item_votes = item.votes;
                answer_name.setTypeface(Typeface.DEFAULT);
                answer_progress.setMax((int) total_votes);
                answer_progress.setProgress(0);
                answer_votes_count.setText(ctx.getResources().getString(R.string.poll_btn_vote));
                answer_progress_value.setVisibility(View.GONE);
                answer_votes_count.setTextColor(Color.parseColor("#6f6f6f"));
                answer_progress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        voteInPoll(item_pos, post, position);
                    }
                });
            }
        }

        public void voteInPoll(int position, WallPost item, int answer) {
            try {
                for (int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                    if (item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment pollAttachment = ((PollAttachment) item.attachments
                                .get(attachment_index).getContent());
                        pollAttachment.user_votes = 1;
                        if (!pollAttachment.answers.get(answer).is_voted) {
                            pollAttachment.answers.get(answer).is_voted = true;
                        }
                        wallPosts.set(position, item);
                        pollAttachment.vote(((NetworkActivity) ctx).ovk_api.wrapper, pollAttachment.answers.get(answer).id);
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }

        public void removeVoteInPoll(int position, WallPost item) {
            try {
                for(int attachment_index = 0; attachment_index < item.attachments.size(); attachment_index++) {
                    if(item.attachments.get(attachment_index).type.equals("poll")) {
                        PollAttachment pollAttachment = ((PollAttachment) item.attachments
                                .get(attachment_index).getContent());
                        pollAttachment.user_votes = 0;
                        for (int i = 0; i < pollAttachment.answers.size(); i++) {
                            if (pollAttachment.answers.get(i).is_voted) {
                                pollAttachment.answers.get(i).is_voted = false;
                            }
                        }
                        wallPosts.set(position, item);
                        pollAttachment.unvote(((NetworkActivity) ctx).ovk_api.wrapper);
                    }
                }
            } catch (Exception ex) {
                Toast.makeText(ctx, R.string.error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setArray(ArrayList<PollAnswer> array) {
        items = array;
    }
}
