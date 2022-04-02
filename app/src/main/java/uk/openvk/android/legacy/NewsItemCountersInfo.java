package uk.openvk.android.legacy;

class NewsItemCountersInfo {
    public int likes;
    public int comments;
    public int reposts;
    public boolean isLiked;
    public boolean isReposted;

    public NewsItemCountersInfo(int likes_count, int comments_count, int reposts_count, boolean likes_selected, boolean reposts_selected) {
        likes = likes_count;
        comments = comments_count;
        reposts = reposts_count;
        isLiked = likes_selected;
        isReposted = reposts_selected;
    }
}
