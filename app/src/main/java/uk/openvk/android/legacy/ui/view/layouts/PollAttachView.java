package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.ui.list.adapters.PollAdapter;

/**
 * Created by Dmitry on 16.10.2022.
 */

public class PollAttachView extends LinearLayout {

    private PollAdapter pollAdapter;
    private LinearLayoutManager llm;

    public PollAttachView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_poll, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    public void createAdapter(Context ctx, int item_pos, ArrayList<PollAnswer> answers, boolean multiple, int user_votes, long total_votes) {
        pollAdapter = new PollAdapter(ctx, item_pos, answers, multiple, user_votes, total_votes);
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
