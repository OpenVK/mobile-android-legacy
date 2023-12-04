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
