package uk.openvk.android.legacy.list_items;

public class InstancesListItem {
    public String server;
    public boolean official;
    public boolean secured;
    public InstancesListItem(String server, boolean official, boolean secured) {
        this.server = server;
        this.official = official;
        this.secured = secured;
    }
}
