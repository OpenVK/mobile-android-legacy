package uk.openvk.android.legacy.user_interface.list_adapters;

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

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.GroupIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.api.models.PollAnswer;

/**
 * Created by Dmitry on 16.10.2022.
 */

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.Holder> {

    private int item_pos;
    private int total_votes;
    private ArrayList<PollAnswer> items = new ArrayList<>();
    private Context ctx;
    private boolean multiple;
    public LruCache memCache;
    private int user_votes;
    private int total_votes_2;

    public PollAdapter(Context context, int item_pos, ArrayList<PollAnswer> answers, boolean multiple, int user_votes, int total_votes) {
        ctx = context;
        this.item_pos = item_pos;
        items = answers;
        this.multiple = multiple;
        this.user_votes = user_votes;
        this.total_votes = total_votes;
        this.total_votes_2 = total_votes;
    }

    @Override
    public PollAdapter.Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PollAdapter.Holder(LayoutInflater.from(ctx).inflate(R.layout.poll_answer_layout, parent, false));
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
                    answer_progress.setProgressDrawable(ctx.getResources().getDrawable(R.drawable.horizontal_progress));
                    answer_votes_count.setTextColor(ctx.getResources().getColor(R.color.ovk_color));
                    item_votes = item.votes + 1;
                    answer_votes_count.setText(String.valueOf(item_votes));
                } else {
                    item_votes = item.votes;
                    answer_name.setTypeface(Typeface.DEFAULT);
                    answer_progress.setProgressDrawable(ctx.getResources().getDrawable(R.drawable.horizontal_progress_2));
                    answer_votes_count.setTextColor(Color.parseColor("#6f6f6f"));
                    answer_votes_count.setText(String.valueOf(item_votes));
                }
                answer_progress.setMax(total_votes);
                answer_progress.setProgress(item.votes);
                double progress = (double) item_votes / (double) total_votes;
                answer_progress_value.setText(String.format("%d%%", (int)(progress * 100)));
                answer_progress.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).removeVoteInPoll(item_pos);
                        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).removeVoteInPoll(item_pos);
                        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).removeVoteInPoll(item_pos);
                        }
                        return true;
                    }
                });
            } else if(user_votes == 0) {
                total_votes = total_votes_2;
                item_votes = item.votes;
                answer_name.setTypeface(Typeface.DEFAULT);
                answer_progress.setMax(total_votes);
                answer_progress.setProgress(0);
                answer_votes_count.setText(ctx.getResources().getString(R.string.poll_btn_vote));
                answer_progress_value.setVisibility(View.GONE);
                answer_votes_count.setTextColor(Color.parseColor("#6f6f6f"));
                answer_progress.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).voteInPoll(item_pos, position);
                        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).voteInPoll(item_pos, position);
                        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).voteInPoll(item_pos, position);
                        }
                    }
                });
            }
        }
    }

    public void setArray(ArrayList<PollAnswer> array) {
        items = array;
    }
}
