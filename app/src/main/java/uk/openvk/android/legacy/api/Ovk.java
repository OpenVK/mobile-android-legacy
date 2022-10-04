package uk.openvk.android.legacy.api;

import org.json.JSONArray;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

/**
 * Created by Dmitry on 01.10.2022.
 */
public class Ovk {
    private JSONParser jsonParser;
    public String version;
    public Ovk() {
        jsonParser = new JSONParser();
    }

    public void getVersion(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Ovk.version");
    }

    public void parseVersion(String response) {
        try {
            version = jsonParser.parseJSON(response).getString("response");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
