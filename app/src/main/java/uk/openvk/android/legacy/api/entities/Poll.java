package uk.openvk.android.legacy.api.entities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.api.attachments.Attachment;
import uk.openvk.android.legacy.api.entities.PollAnswer;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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

public class Poll extends Attachment implements Serializable {
    public long id;
    public long end_date;
    public String question;
    public boolean can_vote;
    public boolean anonymous;
    public boolean multiple;
    public ArrayList<PollAnswer> answers;
    public int user_votes;
    public long votes;
    public Poll(String question, long id, long end_date, boolean multiple, boolean can_vote, boolean anonymous) {
        type = "poll";
        this.question = question;
        this.id = id;
        this.end_date = end_date;
        answers = new ArrayList<PollAnswer>();
        this.multiple = multiple;
        this.can_vote = can_vote;
        this.anonymous = anonymous;
    }

    public Poll() {

    }

    public void vote(OvkAPIWrapper wrapper, long answer_id) {
        wrapper.sendAPIMethod("Polls.addVote", String.format("poll_id=%s&answers_ids=%s", id, answer_id));
    }

    public void unvote(OvkAPIWrapper wrapper) {
        wrapper.sendAPIMethod("Polls.deleteVote", String.format("poll_id=%s", id));
    }

    @Override
    public void serialize(JSONObject object) {
        super.serialize(object);
        try {
            JSONObject poll = new JSONObject();
            poll.put("id", id);
            poll.put("question", question);
            JSONArray answers = new JSONArray();
            for (int i = 0; i < this.answers.size(); i++) {
                PollAnswer answer = this.answers.get(i);
                JSONObject json_answer = new JSONObject();
                json_answer.put("id", answer.id);
                json_answer.put("is_voted", answer.is_voted);
                json_answer.put("rate", answer.is_voted);
                json_answer.put("votes", answer.votes);
                json_answer.put("text", answer.text);
                answers.put(json_answer);
            }
            poll.put("answers", answers);
            poll.put("end_date", end_date);
            object.put("poll", poll);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deserialize(String attach_blob) {
        try {
            super.deserialize(attach_blob);
            JSONObject poll = unserialized_data.getJSONObject("poll");
            id = poll.getLong("id");
            question = poll.getString("question");
            JSONArray answers = poll.getJSONArray("answers");
            this.answers = new ArrayList<>();
            for(int i = 0; i < answers.length(); i++) {
                JSONObject json_answer = answers.getJSONObject(i);
                PollAnswer answer = new PollAnswer();
                answer.id = json_answer.getLong("id");
                answer.is_voted = json_answer.getBoolean("is_voted");
                answer.rate = json_answer.getBoolean("rate") ? 1 : 0;
                answer.votes = json_answer.getInt("votes");
                this.answers.add(answer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
