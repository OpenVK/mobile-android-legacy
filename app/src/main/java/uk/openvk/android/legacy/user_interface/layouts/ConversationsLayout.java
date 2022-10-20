package uk.openvk.android.legacy.user_interface.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.user_interface.list_adapters.ConversationsListAdapter;

public class ConversationsLayout extends LinearLayout {
    private ArrayList<Conversation> conversations;
    private ConversationsListAdapter conversationsAdapter;
    private ListView convListView;

    public ConversationsLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.conversations_layout, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);

        convListView = (ListView) findViewById(R.id.conversations_listview);
    }

    public void createAdapter(Context ctx, ArrayList<Conversation> conversations) {
        this.conversations = conversations;
        conversationsAdapter = new ConversationsListAdapter(ctx, conversations);
        convListView.setAdapter(conversationsAdapter);
    }

    public int getCount() {
        return convListView.getCount();
    }
}
