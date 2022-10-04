package uk.openvk.android.legacy.api.wrappers;

import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.legacy.api.Account;

public class JSONParser {

    public JSONParser() {
    }

    public JSONObject parseJSON(String string) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(string);
        } catch (JSONException ex){
            ex.printStackTrace();
        }
        return jsonObject;
    }

    public Account getAccount(String jsonString) {
        Account account = null;
        JSONObject json = parseJSON(jsonString);
        if(json != null) {
            try {
                JSONObject response = json.getJSONObject("response");
                account = new Account(response.getString("first_name"), response.getString("last_name"), response.getInt("id"), response.getString("status"), response.getString("birthday"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return account;
    }
}
