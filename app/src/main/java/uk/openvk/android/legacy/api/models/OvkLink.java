package uk.openvk.android.legacy.api.models;

/**
 * Created by Dmitry on 12.10.2022.
 */

public class OvkLink {
    public String name;
    public String screen_name;
    public String url;
    public OvkLink() {

    }
    public OvkLink(String name, String screen_name, String url) {
        this.name = name;
        this.screen_name = screen_name;
        this.url = url;
    }
}
