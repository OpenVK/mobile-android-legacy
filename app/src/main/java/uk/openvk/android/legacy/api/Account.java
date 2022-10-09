package uk.openvk.android.legacy.api;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.api.wrappers.JSONParser;
import uk.openvk.android.legacy.api.wrappers.OvkAPIWrapper;

public class Account implements Parcelable {
    public String first_name;
    public String last_name;
    public int id;
    public String status;
    public String birthdate;
    private JSONParser jsonParser;
    private String queue_method;
    private String queue_args;
    private Context ctx;
    private boolean retryConnection;

    public Account(Context ctx) {
        retryConnection = false;
        jsonParser = new JSONParser();
        this.ctx = ctx;
    }

    public Account(String response, Context ctx) {
        retryConnection = false;
        jsonParser = new JSONParser();
        this.ctx = ctx;
        parse(response);
    }

    public Account(String first_name, String last_name, int id, String status, String birthdate) {
        retryConnection = false;
        this.first_name = first_name;
        this.last_name = last_name;
        this.id = id;
        this.status = status;
        this.birthdate = birthdate;
        jsonParser = new JSONParser();
    }

    protected Account(Parcel in) {
        first_name = in.readString();
        last_name = in.readString();
        id = in.readInt();
        status = in.readString();
        birthdate = in.readString();
    }

    public static final Creator<Account> CREATOR = new Creator<Account>() {
        @Override
        public Account createFromParcel(Parcel in) {
            return new Account(in);
        }

        @Override
        public Account[] newArray(int size) {
            return new Account[size];
        }
    };

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
        if(retryConnection) {
            if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                ((AppActivity) ctx).retryConnection(queue_method, queue_args);
            }
        }
    }

    public void getProfileInfo(OvkAPIWrapper ovk) {
        ovk.sendAPIMethod("Account.getProfileInfo");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(first_name);
        parcel.writeString(last_name);
        parcel.writeInt(id);
        parcel.writeString(status);
        parcel.writeString(birthdate);
    }

    public void addQueue(String method, String args) {
        queue_method = method;
        queue_args = args;
        this.retryConnection = true;
    }
}
