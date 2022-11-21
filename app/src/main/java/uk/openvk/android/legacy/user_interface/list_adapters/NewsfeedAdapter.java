package uk.openvk.android.legacy.user_interface.list_adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
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
import uk.openvk.android.legacy.api.attachments.PhotoAttachment;
import uk.openvk.android.legacy.api.attachments.PollAttachment;
import uk.openvk.android.legacy.user_interface.activities.AppActivity;
import uk.openvk.android.legacy.user_interface.activities.GroupIntentActivity;
import uk.openvk.android.legacy.user_interface.activities.ProfileIntentActivity;
import uk.openvk.android.legacy.api.models.OvkLink;
import uk.openvk.android.legacy.user_interface.layouts.PollLayout;
import uk.openvk.android.legacy.api.models.WallPost;

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.Holder> {

    private ArrayList<WallPost> items = new ArrayList<>();
    private Context ctx;
    public LruCache memCache;

    public NewsfeedAdapter(Context context, ArrayList<WallPost> posts) {
        ctx = context;
        items = posts;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.newsfeed_item, parent, false));
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

    public WallPost getItem(int position) {
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
        public final LinearLayout repost_info;
        public final TextView original_poster_name;
        public final TextView original_post_info;
        public final TextView original_post_text;
        public final TextView likes_counter;
        public final TextView reposts_counter;
        public final TextView comments_counter;
        public final View convertView;
        public final ImageView avatar;
        private final ProgressBar photo_progress;
        private final TextView error_label;
        private final PollLayout pollLayout;
        private final ImageView original_post_photo;
        private final PollLayout original_post_poll;
        private final TextView expand_text_btn;
        private final TextView repost_expand_text_btn;
        private boolean likeAdded = false;
        private boolean likeDeleted = false;

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
            this.pollLayout = ((PollLayout) convertView.findViewById(R.id.poll_layout));
            this.repost_info = ((LinearLayout) convertView.findViewById(R.id.post_attach_container));
            this.original_poster_name = ((TextView) convertView.findViewById(R.id.post_retweet_name));
            this.original_post_info = ((TextView) convertView.findViewById(R.id.post_retweet_time));
            this.original_post_text = ((TextView) convertView.findViewById(R.id.post_retweet_text));
            this.original_post_photo = (ImageView) view.findViewById(R.id.repost_photo);
            this.original_post_poll = (PollLayout) view.findViewById(R.id.repost_poll_layout);
            this.expand_text_btn = (TextView) view.findViewById(R.id.expand_text_btn);
            this.repost_expand_text_btn = (TextView) view.findViewById(R.id.repost_expand_text_btn);
        }

        void bind(final int position) {
            final WallPost item = getItem(position);
            if(item.verified_author) {
                String name = item.name;
                SpannableStringBuilder sb = new SpannableStringBuilder(name);
                ImageSpan imageSpan;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    imageSpan = new ImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black, DynamicDrawableSpan.ALIGN_BOTTOM);
                } else {
                    imageSpan = new ImageSpan(ctx.getApplicationContext(), R.drawable.verified_icon_black, DynamicDrawableSpan.ALIGN_BASELINE);
                }
                sb.setSpan(imageSpan, name.length() - 1, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                poster_name.setText(item.name);
            } else {
                poster_name.setText(item.name);
            }
            post_info.setText(item.info);
            if(item.text.length() > 500) {
                expand_text_btn.setVisibility(View.GONE);
                post_text.setVisibility(View.VISIBLE);
                Pattern pattern = Pattern.compile("\\[(.+?)\\]");
                Matcher matcher = pattern.matcher(item.text);
                boolean regexp_search = matcher.find();
                String text = item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
                int regexp_results = 0;
                while(regexp_search) {
                    if(regexp_results == 0) {
                        text = text.replace("\n", "<br>");
                    }
                    String block = matcher.group();
                    OvkLink link = new OvkLink();
                    String[] markup = block.replace("[", "").replace("]", "").split("\\|");
                    link.screen_name = markup[0];
                    if(markup.length == 2) {
                        if (markup[0].startsWith("id")) {
                            link.url = String.format("openvk://profile/%s", markup[0]);
                            link.name = markup[1];
                        } else if (markup[0].startsWith("club")) {
                            link.url = String.format("openvk://group/%s", markup[0]);
                            link.name = markup[1];
                        }
                        link.name = markup[1];
                        if (markup[0].startsWith("id") || markup[0].startsWith("club")) {
                            text = text.replace(block, String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                        }
                    }
                    regexp_results = regexp_results + 1;
                    regexp_search = matcher.find();
                }
                text = text.substring(0, 500) + "...";
                if(regexp_results > 0) {
                    post_text.setText(Html.fromHtml(text));
                    post_text.setAutoLinkMask(0);
                } else {
                    post_text.setText(text);
                }
                expand_text_btn.setVisibility(View.VISIBLE);
                expand_text_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).openWallComments(position, null);
                        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).openWallComments(position, null);
                        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).openWallComments(position, null);
                        }
                    }
                });
            } else if(item.text.length() > 0) {
                expand_text_btn.setVisibility(View.GONE);
                post_text.setVisibility(View.VISIBLE);
                Pattern pattern = Pattern.compile("\\[(.+?)\\]");
                Matcher matcher = pattern.matcher(item.text);
                boolean regexp_search = matcher.find();
                String text = item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
                int regexp_results = 0;
                while(regexp_search) {
                    if(regexp_results == 0) {
                        text = text.replace("\n", "<br>");
                    }
                    String block = matcher.group();
                    OvkLink link = new OvkLink();
                    String[] markup = block.replace("[", "").replace("]", "").split("\\|");
                    link.screen_name = markup[0];
                    if(markup.length == 2) {
                        if (markup[0].startsWith("id")) {
                            link.url = String.format("openvk://profile/%s", markup[0]);
                            link.name = markup[1];
                        } else if (markup[0].startsWith("club")) {
                            link.url = String.format("openvk://group/%s", markup[0]);
                            link.name = markup[1];
                        }
                        link.name = markup[1];
                        if (markup[0].startsWith("id") || markup[0].startsWith("club")) {
                            text = text.replace(block, String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                        }
                    }
                    regexp_results = regexp_results + 1;
                    regexp_search = matcher.find();
                }
                if(regexp_results > 0) {
                    post_text.setText(Html.fromHtml(text));
                    post_text.setAutoLinkMask(0);
                } else {
                    post_text.setText(text);
                }
                post_text.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                expand_text_btn.setVisibility(View.GONE);
                post_text.setVisibility(View.GONE);
            }

            if(item.repost != null) {
                repost_info.setVisibility(View.VISIBLE);
                original_poster_name.setText(item.repost.name);
                original_post_info.setText(item.repost.time);
                String repost_text = item.repost.newsfeed_item.text.replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
                if(repost_text.length() > 500) {
                    original_post_text.setText(String.format("%s...", repost_text.substring(0, 500)));
                    repost_expand_text_btn.setVisibility(View.VISIBLE);
                } else {
                    original_post_text.setText(repost_text);
                    repost_expand_text_btn.setVisibility(View.GONE);
                }
                for(int i = 0; i < item.repost.newsfeed_item.attachments.size(); i++) {
                    if (item.repost.newsfeed_item.attachments.get(i).status.equals("loading")) {
                        try {
                            photo_progress.setVisibility(View.VISIBLE);
                        } catch (Exception ex) {
                        }
                        original_post_photo.setImageBitmap(null);
                    } else if (item.repost.newsfeed_item.attachments.get(i).status.equals("not_supported")) {
                        error_label.setText(ctx.getResources().getString(R.string.not_supported));
                        error_label.setVisibility(View.VISIBLE);
                    } else if (item.repost.newsfeed_item.attachments.get(i).status.equals("done") && item.repost.newsfeed_item.attachments.get(i).type.equals("photo")) {
                        if (item.repost.newsfeed_item.attachments.get(i).getContent() != null) {
                            original_post_photo.setImageBitmap(((PhotoAttachment) item.repost.newsfeed_item.attachments.get(i).getContent()).photo);
                            original_post_photo.setVisibility(View.VISIBLE);
                        }
                    } else if (item.repost.newsfeed_item.attachments.get(i).type.equals("poll")) {
                        if (item.repost.newsfeed_item.attachments.get(i).getContent() != null) {
                            PollAttachment pollAttachment = ((PollAttachment) item.repost.newsfeed_item.attachments.get(i).getContent());
                            original_post_poll.createAdapter(ctx, position, pollAttachment.answers, pollAttachment.multiple, pollAttachment.user_votes, pollAttachment.votes);
                            original_post_poll.setPollInfo(pollAttachment.question, pollAttachment.anonymous, pollAttachment.end_date);
                            original_post_poll.setVisibility(View.VISIBLE);
                        }
                    }
                }
                repost_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).openWallRepostComments(position, view);
                        } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).openWallRepostComments(position, view);
                        } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).openWallRepostComments(position, view);
                        }
                    }
                });
            } else {
                repost_info.setVisibility(View.GONE);
            }

            error_label.setVisibility(View.GONE);
            photo_progress.setVisibility(View.GONE);
            post_photo.setVisibility(View.GONE);
            pollLayout.setVisibility(View.GONE);

            for(int i = 0; i < item.attachments.size(); i++) {
                if (item.attachments.get(i).status.equals("loading")) {
                    photo_progress.setVisibility(View.VISIBLE);
                    post_photo.setImageBitmap(null);
                } else if (item.attachments.get(i).status.equals("not_supported")) {
                    error_label.setText(ctx.getResources().getString(R.string.not_supported));
                    error_label.setVisibility(View.VISIBLE);
                } else if (item.attachments.get(i).status.equals("done") && item.attachments.get(i).type.equals("photo")) {
                    if (item.attachments.get(i).getContent() != null) {
                        post_photo.setImageBitmap(((PhotoAttachment) item.attachments.get(0).getContent()).photo);
                        post_photo.setVisibility(View.VISIBLE);
                        post_photo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                                    ((AppActivity) ctx).viewPhotoAttachment(position);
                                } else if(ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                                    ((ProfileIntentActivity) ctx).viewPhotoAttachment(position);
                                } else if(ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                                    ((GroupIntentActivity) ctx).viewPhotoAttachment(position);
                                }
                            }
                        });
                    }
                } else if (item.attachments.get(i).type.equals("poll")) {
                    if (item.attachments.get(i).getContent() != null) {
                        PollAttachment pollAttachment = ((PollAttachment) item.attachments.get(i).getContent());
                        pollLayout.createAdapter(ctx, position, pollAttachment.answers, pollAttachment.multiple, pollAttachment.user_votes, pollAttachment.votes);
                        pollLayout.setPollInfo(pollAttachment.question, pollAttachment.anonymous, pollAttachment.end_date);
                        pollLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            likes_counter.setText("" + item.counters.likes);
            reposts_counter.setText("" + item.counters.reposts);
            comments_counter.setText("" + item.counters.comments);

            if(item.counters.isLiked) {
                likes_counter.setSelected(true);
            } else {
                likes_counter.setSelected(false);
            }

            if(item.counters.enabled) {
                likes_counter.setEnabled(true);
                if(item.counters.isLiked && likeAdded) {
                    likes_counter.setText("" + (item.counters.likes + 1));
                } else if(!item.counters.isLiked && likeDeleted) {
                    likes_counter.setText("" + (item.counters.likes - 1));
                } else {
                    likes_counter.setText("" + (item.counters.likes));
                }
            } else {
                likes_counter.setEnabled(false);
            }

            Bitmap author_avatar = item.avatar;
            if(author_avatar != null) {
                avatar.setImageBitmap(author_avatar);
            } else {
                avatar.setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
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
                        if(!likeAdded) {
                            likeDeleted = true;
                        }
                        if (ctx.getClass().getSimpleName().equals("ProfileIntentActivity")) {
                            ((ProfileIntentActivity) ctx).deleteLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("GroupIntentActivity")) {
                            ((GroupIntentActivity) ctx).deleteLike(position, "post", view);
                        } else if (ctx.getClass().getSimpleName().equals("AppActivity")) {
                            ((AppActivity) ctx).deleteLike(position, "post", view);
                        }
                    } else {
                        if(!likeDeleted) {
                            likeAdded = true;
                        }
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

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }

    public void setArray(ArrayList<WallPost> array) {
        items = array;
    }
}