package uk.openvk.android.legacy.list_adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.activities.GroupIntentActivity;
import uk.openvk.android.legacy.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.api.models.OvkLink;
import uk.openvk.android.legacy.list_items.NewsfeedItem;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.Holder> {

    private ArrayList<NewsfeedItem> items = new ArrayList<>();
    private Context ctx;
    public LruCache memCache;

    public NewsfeedAdapter(Context context, ArrayList<NewsfeedItem> posts) {
        ctx = context;
        items = posts;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.news_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
        holder.post_photo.setImageBitmap(null);
        holder.post_photo.setVisibility(View.GONE);
    }

    public NewsfeedItem getItem(int position) {
       return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final TextView poster_name;
        public final TextView post_info;
        public final TextView post_text;
        public final ImageView post_photo;
        public final TextView likes_counter;
        public final TextView reposts_counter;
        public final TextView comments_counter;
        public final View convertView;
        public final ImageView avatar;
        private final ProgressBar photo_progress;
        private final TextView error_label;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.poster_name = (TextView) view.findViewById(R.id.poster_name_view);
            this.post_info = (TextView) view.findViewById(R.id.post_info_view);
            this.post_text = (TextView) view.findViewById(R.id.post_view);
            this.post_photo = (ImageView) view.findViewById(R.id.post_photo);
            this.likes_counter = (TextView) view.findViewById(R.id.post_likes);
            this.reposts_counter = (TextView) view.findViewById(R.id.post_reposts);
            this.comments_counter = (TextView) view.findViewById(R.id.post_comments);
            this.avatar = (ImageView) view.findViewById(R.id.author_avatar);
            this.photo_progress = ((ProgressBar) view.findViewById(R.id.photo_progress));
            this.error_label = ((TextView) convertView.findViewById(R.id.error_label));
        }

        void bind(final int position) {
            final NewsfeedItem item = getItem(position);
            poster_name.setText(item.name);
            post_info.setText(item.info);
            if(item.text.length() > 0) {
                post_text.setVisibility(View.VISIBLE);
                post_text.setText(item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\""));
                post_text.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                post_text.setVisibility(View.GONE);
            }

            if(item.photo != null) {
                item.attachment_status = "photo";
            }

            Pattern pattern = Pattern.compile("[.*?]");
            Matcher matcher = pattern.matcher(item.text);
            OvkLink link = new OvkLink();
            while(matcher.find()) {
                if(matcher.groupCount() > 0) {
                    for(int i = 0; i < matcher.groupCount(); i++) {
                        String[] markup = matcher.group(i).split("|");
                        Log.d("Matcher", matcher.group(i));
                        if(markup.length >= 2) {
                            link.screen_name = markup[0];
                            if (matcher.group(i).startsWith("id")) {
                                link.url = String.format("openvk://profile/%s", markup[0]);
                            } else if (matcher.group(i).startsWith("club")) {
                                link.url = String.format("openvk://group/%s", markup[0]);
                            }
                            link.name = markup[1];
                            if(matcher.group(i).startsWith("id") || matcher.group(i).startsWith("club")) {
                                item.text = item.text.replace(matcher.group(i), String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                                post_text.setText(Html.fromHtml(item.text));
                            }
                        }
                    }
                }
            }
            if(item.attachment_status.equals("none")) {
                post_photo.setImageBitmap(null);
                post_photo.setVisibility(View.GONE);
                error_label.setVisibility(View.GONE);
            } else if(item.attachment_status.equals("loading")) {
                photo_progress.setVisibility(View.VISIBLE);
                post_photo.setVisibility(View.GONE);
                error_label.setVisibility(View.GONE);
            } else if(item.attachment_status.equals("not_supported")) {
                error_label.setText(ctx.getResources().getString(R.string.not_supported));
                error_label.setVisibility(View.VISIBLE);
                photo_progress.setVisibility(View.GONE);
                post_photo.setVisibility(View.GONE);
            } else if(item.attachment_status.equals("photo")) {
                error_label.setVisibility(View.GONE);
                photo_progress.setVisibility(View.GONE);
                if(item.photo != null) {
                    post_photo.setImageBitmap(item.photo);
                    post_photo.setVisibility(View.VISIBLE);
                }
            }

            if(item.counters.isLiked == true) {
                likes_counter.setSelected(true);
            } else {
                likes_counter.setSelected(false);
            }

            likes_counter.setText("" + item.counters.likes);
            reposts_counter.setText("" + item.counters.reposts);
            comments_counter.setText("" + item.counters.comments);
            Bitmap author_avatar = item.avatar;
            if(author_avatar != null) {
                avatar.setImageBitmap(author_avatar);
            }

            ((LinearLayout) convertView.findViewById(R.id.poster_ll)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).showAuthorPage(position);
                    } else if (ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                        ((GroupIntentActivity) ctx).showAuthorPage(position);
                    } else if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).showAuthorPage(position);
                    }
                }
            });

            likes_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.counters.isLiked) {
                        if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).deleteLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).deleteLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).deleteLike(position, "post", view);
                        }
                    } else {
                        if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).addLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).addLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).addLike(position, "post", view);
                        }
                    }
                }
            });

            comments_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                        ((AppActivity) ctx).openWallComments(position, view);
                    } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                        ((ProfileIntentActivity) ctx).openWallComments(position, view);
                    } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                        ((GroupIntentActivity) ctx).openWallComments(position, view);
                    }
                }
            });
        }
    }

    public void setArray(ArrayList<NewsfeedItem> array) {
        items = array;
    }
}