package uk.openvk.android.legacy.api.models;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/**
 * Created by Dmitry on 16.10.2022.
 */

public class Poll {
    public int id;
    public long end_date;
    public String question;
    public boolean can_vote;
    public boolean anonymous;
    public boolean multiple;
    public ArrayList<PollAnswer> answers;
    public int user_votes;
    public int votes;
    public Poll(String question, int id, long end_date, boolean multiple, boolean can_vote, boolean anonymous) {
        this.question = question;
        this.id = id;
        this.end_date = end_date;
        answers = new ArrayList<PollAnswer>();
        this.multiple = multiple;
        this.can_vote = can_vote;
        this.anonymous = anonymous;
    }

    public void vote(OvkAPIWrapper ovk, int poll_id, int answer_id) {
        ovk.sendAPIMethod("Polls.addVote", String.format("poll_id=%d&answers_ids=%d", poll_id, answer_id));
    }

    public void unvote(OvkAPIWrapper ovk, int poll_id) {
        ovk.sendAPIMethod("Polls.deleteVote", String.format("poll_id=%d", poll_id));
    }
}
