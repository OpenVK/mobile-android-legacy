package uk.openvk.android.legacy.ui;

import uk.openvk.android.legacy.ui.core.fragments.app.ConversationsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.FriendsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.GroupsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.MainSettingsFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.NewsfeedFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.NotesFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.PhotosFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.VideosFragment;

/*  Copyleft © 2022, 2023 OpenVK Team
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

public class FragmentHub {
    public NotesFragment notesFragment;
    public GroupsFragment groupsFragment;
    public VideosFragment videosFragment;
    public PhotosFragment photosFragment;
    public FriendsFragment friendsFragment;
    public NewsfeedFragment newsfeedFragment;
    public ProfileFragment profileFragment;
    public ConversationsFragment conversationsFragment;
    public MainSettingsFragment mainSettingsFragment;

    public FragmentHub() {
        profileFragment = new ProfileFragment();
        newsfeedFragment = new NewsfeedFragment();
        friendsFragment = new FriendsFragment();
        photosFragment = new PhotosFragment();
        videosFragment = new VideosFragment();
        groupsFragment = new GroupsFragment();
        notesFragment = new NotesFragment();
        conversationsFragment = new ConversationsFragment();
        mainSettingsFragment = new MainSettingsFragment();
    }
}
