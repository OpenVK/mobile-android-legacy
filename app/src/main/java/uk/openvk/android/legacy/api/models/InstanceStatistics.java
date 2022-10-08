package uk.openvk.android.legacy.api.models;

/**
 * Created by Dmitry on 07.10.2022.
 */

public class InstanceStatistics {
    public int users_count;
    public int online_users_count;
    public int active_users_count;
    public int groups_count;
    public int wall_posts_count;
    public InstanceStatistics(int users_count, int online_users_count, int active_users_count, int groups_count, int wall_posts_count) {
        this.users_count = users_count;
        this.online_users_count = online_users_count;
        this.active_users_count = active_users_count;
        this.groups_count = groups_count;
        this.wall_posts_count = wall_posts_count;
    }
}
