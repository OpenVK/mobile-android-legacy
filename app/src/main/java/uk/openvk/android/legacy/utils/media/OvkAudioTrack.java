package uk.openvk.android.legacy.utils.media;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 *
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class OvkAudioTrack extends OvkMediaTrack {
    public String codec_name;
    public long frame_size;
    public long sample_rate;
    public long bitrate;
    public int channels;
    public OvkAudioTrack() {

    }
}
