package uk.openvk.android.legacy.ui.core.fragments.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.Account;
import uk.openvk.android.legacy.api.models.Conversation;
import uk.openvk.android.legacy.ui.list.adapters.ConversationsListAdapter;

public class ConversationsFragment extends Fragment {
    private ArrayList<Conversation> conversations;
    private ConversationsListAdapter conversationsAdapter;
    private ListView convListView;
    private Account account;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.conversations_layout, container, false);
        convListView = view.findViewById(R.id.conversations_listview);
        return view;
    }

    public void createAdapter(Context ctx, ArrayList<Conversation> conversations, Account account) {
        this.conversations = conversations;
        this.account = account;
        conversationsAdapter = new ConversationsListAdapter(ctx, this.conversations, account);
        convListView.setAdapter(conversationsAdapter);
    }

    public int getCount() {
        return convListView.getCount();
    }

    public void loadAvatars(ArrayList<Conversation> conversations_list) {
        try {
            for (int i = 0; i < conversations_list.size(); i++) {
                Conversation conversation = conversations_list.get(i);
                if (conversation.avatar_url.length() > 0) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/conversations_avatars/avatar_%s", getContext().getCacheDir(),
                            conversation.peer_id), options);
                    if (bitmap != null) {
                        conversation.avatar = bitmap;
                        conversations_list.set(i, conversation);
                    }
                }
            }
            conversations = conversations_list;
            conversationsAdapter = new ConversationsListAdapter(getContext(), conversations, account);
            convListView.setAdapter(conversationsAdapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
