package uk.openvk.android.legacy;

public class GroupPostInfo {
    int postId;
    int postAuthorId;
    String ownerTitle;
    public GroupPostInfo(int post_id, int post_author_id, String author) {
        postId = post_id;
        postAuthorId = post_author_id;
        ownerTitle = author;
    }
}
