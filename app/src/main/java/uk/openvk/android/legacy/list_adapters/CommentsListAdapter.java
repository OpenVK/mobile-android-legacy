package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.Comment;

public class CommentsListAdapter extends RecyclerView.Adapter<CommentsListAdapter.Holder> {

    private ArrayList<Comment> items = new ArrayList<>();
    private Context ctx;
    public LruCache memCache;

    public CommentsListAdapter(Context context, ArrayList<Comment> comments) {
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

    public Comment getItem(int position) {
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
            Comment item = getItem(position);
            author_name.setText(item.author);
            Date date = new Date(TimeUnit.SECONDS.toMillis(item.date));
            comment_info.setText(new SimpleDateFormat("d MMMM yyyy").format(date) + " " + ctx.getResources().getString(R.string.date_at) + " " + new SimpleDateFormat("HH:mm").format(date));
            if(item.text.length() > 0) {
                comment_text.setVisibility(View.VISIBLE);
                comment_text.setText(item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\""));
                comment_text.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                comment_text.setVisibility(View.GONE);
            }
            if(item.avatar != null) {
                author_avatar.setImageBitmap(item.avatar);
            }
        }
    }

    public void setArray(ArrayList<Comment> array) {
        items = array;
    }
}