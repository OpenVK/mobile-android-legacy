package uk.openvk.android.legacy;

class NewsItemCountersInfo {
    public int likes;
    public int comments;
    public int reposts;

    public NewsItemCountersInfo(int likes_count, int comments_count, int reposts_count) {
        likes = likes_count;
        comments = comments_count;
        reposts = reposts_count;
    }
}
