package uk.openvk.android.legacy.api.entities;

import java.io.Serializable;

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

public class PollAnswer implements Serializable {
    public int id;
    public int rate;
    public int votes;
    public String text;
    public boolean is_voted;
    public PollAnswer(int id, int rate, int votes, String text) {
        this.id = id;
        this.rate = rate;
        this.votes = votes;
        this.text = text;
    }
}
