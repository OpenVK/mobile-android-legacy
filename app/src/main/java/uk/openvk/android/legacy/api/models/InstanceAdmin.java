package uk.openvk.android.legacy.api.models;

/**
 * Created by Dmitry on 07.10.2022.
 */

public class InstanceAdmin {
    public String first_name;
    public String last_name;
    public int id;
    public InstanceAdmin(String first_name, String last_name, int id) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
    }
}
