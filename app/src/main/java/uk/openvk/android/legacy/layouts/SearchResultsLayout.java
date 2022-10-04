package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.QuickSearchActivity;
import uk.openvk.android.legacy.api.Users;
import uk.openvk.android.legacy.api.models.User;
import uk.openvk.android.legacy.list_adapters.FriendsListAdapter;
import uk.openvk.android.legacy.list_adapters.SearchResultAdapter;

public class SearchResultsLayout extends LinearLayout {
    private ListView people_listview;
    private ArrayList<User> users;
    private SearchResultAdapter searchResultAdapter;

    public SearchResultsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.search_results, null);

        this.addView(view);
    }

    public void createAdapter(Context ctx, ArrayList<User> users) {
        this.users = users;
        searchResultAdapter = new SearchResultAdapter(ctx, users);
        people_listview = (ListView) findViewById(R.id.people_listview);
        people_listview.setAdapter(searchResultAdapter);
    }
}
