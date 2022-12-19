package uk.openvk.android.legacy.api.attachments;

import java.util.ArrayList;

import uk.openvk.android.legacy.api.models.PollAnswer;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class PollAttachment {
    public long id;
    public long end_date;
    public String question;
    public boolean can_vote;
    public boolean anonymous;
    public boolean multiple;
    public ArrayList<PollAnswer> answers;
    public int user_votes;
    public long votes;
    public PollAttachment(String question, long id, long end_date, boolean multiple, boolean can_vote, boolean anonymous) {
        this.question = question;
        this.id = id;
        this.end_date = end_date;
        answers = new ArrayList<PollAnswer>();
        this.multiple = multiple;
        this.can_vote = can_vote;
        this.anonymous = anonymous;
    }

    public PollAttachment() {

    }

    public void vote(OvkAPIWrapper ovk, int answer_id) {
        ovk.sendAPIMethod("Polls.addVote", String.format("poll_id=%d&answers_ids=%d", id, answer_id));
    }

    public void unvote(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Polls.deleteVote", String.format("poll_id=%d", id));
    }
}
