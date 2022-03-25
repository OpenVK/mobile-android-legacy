package uk.openvk.android.legacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.Thread.sleep;

public class NewsLinearLayout extends LinearLayout {
    public String auth_token;
    public TextView titlebar_title;
    public HttpURLConnection httpConnection;
    public HttpsURLConnection httpsConnection;
    public String server;
    public String state;
    public ProgressDialog connectionDialog;
    public StringBuilder response_sb;
    public JSONObject json_response;
    public JSONObject json_response_user;
    public JSONArray newsfeed;
    public String connectionErrorString;
    public boolean creating_another_activity;
    public PopupWindow popup_menu;
    public ArrayList<NewsListItem> newsListItemArray;
    public NewsListAdapter newsListAdapter;
    public int postAuthorId;
    public Thread socketThread;
    public Thread sslSocketThread;
    public String send_request;
    public ListView news_listview;
    public int news_item_count;
    public int news_item_index;
    public Boolean inputStream_isClosed;
    public SharedPreferences global_sharedPreferences;
    public ArrayList<Integer> post_author_ids;
    public StringBuilder post_author_ids_sb;
    public StringBuilder post_group_ids_sb;

    public NewsLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.news_layout, null);

        this.addView(view);

    }
}