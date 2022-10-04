package uk.openvk.android.legacy.api.models;

import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.JSONParser;

/**
 * Created by Dmitry on 27.09.2022.
 */
public class Error {
    public String description;
    public int code;
    private JSONParser jsonParser;

    public Error() {
        jsonParser = new JSONParser();
    }

    public Error(String response) {
        jsonParser = new JSONParser();
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                description = json.getString("error_msg");
                code = json.getInt("error_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void parse(String response) {
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                description = json.getString("error_msg");
                code = json.getInt("error_code");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public Error(String description, int code) {
        this.description = description;
        this.code = code;
    }
}
