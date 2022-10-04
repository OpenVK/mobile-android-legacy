package uk.openvk.android.legacy.api;

import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Account {
    public String first_name;
    public String last_name;
    public int id;
    public String status;
    public String birthdate;
    private JSONParser jsonParser;

    public Account() {
        jsonParser = new JSONParser();
    }

    public Account(String response) {
        jsonParser = new JSONParser();
        parse(response);
    }

    public Account(String first_name, String last_name, int id, String status, String birthdate) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
        this.status = status;
        this.birthdate = birthdate;
        jsonParser = new JSONParser();
    }

    public void parse(String response) {
        try {
            JSONObject json = jsonParser.parseJSON(response);
            if(json != null) {
                JSONObject account = json.getJSONObject("response");
                first_name = account.getString("first_name");
                last_name = account.getString("last_name");
                id = account.getInt("id");
                status = account.getString("status");
                birthdate = account.getString("bdate");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void getProfileInfo(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Account.getProfileInfo");
    }
}
