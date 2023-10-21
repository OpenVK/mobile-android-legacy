package uk.openvk.android.legacy.api.attachments;

import java.util.ArrayList;

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

    public void vote(OvkAPIWrapper wrapper, int answer_id) {
        wrapper.sendAPIMethod("Polls.addVote", String.format("poll_id=%s&answers_ids=%s", id, answer_id));
    }

    public void unvote(OvkAPIWrapper wrapper) {
        wrapper.sendAPIMethod("Polls.deleteVote", String.format("poll_id=%s", id));
    }
}
