package uk.openvk.android.legacy.items;

public class UserPostInfo {
    int postId;
    int postUserId;
    int postAuthorId;
    String ownerTitle;
    public UserPostInfo(int post_id, int post_group_id, int post_author_id, String owner) {
        postId = post_id;
        postUserId = post_group_id;
        postAuthorId = post_author_id;
        ownerTitle = owner;
    }
}
