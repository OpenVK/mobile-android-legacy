package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.list_items.CommentsListItem;
import uk.openvk.android.legacy.list_items.NewsListItem;
import uk.openvk.android.legacy.listeners.SwipeListener;

public class CommentsListAdapter extends RecyclerView.Adapter<CommentsListAdapter.Holder> {

    private ArrayList<CommentsListItem> items = new ArrayList<>();
    private Context ctx;
    public LruCache memCache;

    public CommentsListAdapter(Context context, ArrayList<CommentsListItem> comments) {
        ctx = context;
        items = comments;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.comment_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
    }

    public CommentsListItem getItem(int position) {
       return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final TextView author_name;
        public final TextView comment_info;
        public final TextView comment_text;
        public final View convertView;
        public final ImageView author_avatar;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.author_name = view.findViewById(R.id.comm_author);
            this.comment_info = view.findViewById(R.id.comm_time);
            this.comment_text = view.findViewById(R.id.comment_text);
            this.author_avatar = view.findViewById(R.id.author_avatar);
        }

        void bind(final int position) {
            CommentsListItem item = getItem(position);
            author_name.setText(item.author);
            comment_info.setText(item.info);
            if(item.text.length() > 0) {
                comment_text.setVisibility(View.VISIBLE);
                comment_text.setText(item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\""));
                comment_text.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                comment_text.setVisibility(View.GONE);
            }

            Bitmap avatar = item.getAvatar();
            if(author_avatar != null) {
                author_avatar.setImageBitmap(avatar);
            } else {
                author_avatar.setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
            }
        }
    }

    public void setArray(ArrayList<CommentsListItem> array) {
        items = array;
    }
}