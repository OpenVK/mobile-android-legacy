package uk.openvk.android.legacy.ui.list.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.entities.Comment;
import uk.openvk.android.legacy.api.entities.OvkLink;
import uk.openvk.android.legacy.api.entities.Photo;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.core.activities.PhotoViewerActivity;
import uk.openvk.android.legacy.core.activities.WallPostActivity;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

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
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_comment, parent, false));
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
        public final View divider;
        private final TextView expand_text_btn;
        private final TextView reply_btn;
        private final ImageView comment_photo;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.author_name = view.findViewById(R.id.comm_author);
            this.comment_info = view.findViewById(R.id.comm_time);
            this.comment_text = view.findViewById(R.id.comment_text);
            this.author_avatar = view.findViewById(R.id.author_avatar);
            this.divider = view.findViewById(R.id.divider);
            this.expand_text_btn = view.findViewById(R.id.expand_text_btn);
            this.reply_btn = view.findViewById(R.id.post_reply);
            this.comment_photo = view.findViewById(R.id.comm_photo);
        }

        @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
        void bind(final int position) {
            final Comment item = getItem(position);
            author_name.setText(item.author);
            Date date = new Date(TimeUnit.SECONDS.toMillis(item.date));
            comment_info.setText(new SimpleDateFormat("dd.MM.yyyy").format(date)
                    + " " + ctx.getResources().getString(R.string.date_at) + " " +
                    new SimpleDateFormat("HH:mm").format(date));
            reply_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ctx.getClass().getSimpleName().equals("WallPostActivity")) {
                        ((WallPostActivity) ctx).addAuthorMention(position);
                    }
                }
            });
            if (item.text.length() > 0) {
                comment_text.setVisibility(View.VISIBLE);
                Pattern pattern = Pattern.compile("\\[(.+?)\\]|" +
                        "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{1,256}" +
                        "\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
                Matcher matcher = pattern.matcher(item.text);
                boolean regexp_search = matcher.find();
                String text = item.text.replaceAll("&lt;", "<")
                        .replaceAll("&gt;", ">")
                        .replaceAll("&amp;", "&").replaceAll("&quot;",
                                "\"");
                int regexp_results = 0;
                while (regexp_search) {
                    if (regexp_results == 0) {
                        text = text.replace("\n", "<br>");
                    }
                    String block = matcher.group();
                    if (block.startsWith("[") && block.endsWith("]")) {
                        OvkLink link = new OvkLink();
                        String[] markup = block.replace("[", "")
                                .replace("]", "").split("\\|");
                        link.screen_name = markup[0];
                        if (markup.length == 2) {
                            if (markup[0].startsWith("id")) {
                                link.url = String.format("openvk://profile/%s", markup[0]);
                                link.name = markup[1];
                            } else if (markup[0].startsWith("club")) {
                                link.url = String.format("openvk://group/%s", markup[0]);
                                link.name = markup[1];
                            }
                            link.name = markup[1];
                            if (markup[0].startsWith("id") || markup[0].startsWith("club")) {
                                text = text.replace(block, String.format("<a href=\"%s\">%s</a>",
                                        link.url, link.name));
                            }
                        }
                    } else if (block.startsWith("https://") || block.startsWith("http://")) {
                        text = text.replace(block, String.format("<a href=\"%s\">%s</a>", block, block));
                    }
                    regexp_results = regexp_results + 1;
                    regexp_search = matcher.find();
                }

                String[] lines = text.split("\r\n|\r|\n");
                if (lines.length > 8 && text.length() <= 500) {
                    String text_llines = "";
                    for (int line_no = 0; line_no < 8; line_no++) {
                        if (line_no == 7) {
                            text_llines += String.format("%s...", lines[line_no]);
                        } else {
                            text_llines += String.format("%s\r\n", lines[line_no]);
                        }
                    }
                    if (regexp_results > 0) {
                        comment_text.setText(Html.fromHtml(text_llines));
                        comment_text.setAutoLinkMask(0);
                    } else {
                        comment_text.setText(text_llines);
                    }
                    expand_text_btn.setVisibility(View.VISIBLE);
                    final int finalRegexp_results = regexp_results;
                    final String finalText = text;
                    expand_text_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (finalRegexp_results > 0) {
                                comment_text.setText(Html.fromHtml(finalText));
                                comment_text.setAutoLinkMask(0);
                            } else {
                                comment_text.setText(finalText);
                            }
                            expand_text_btn.setVisibility(View.GONE);
                        }
                    });
                } else if (text.length() > 500) {
                    if (regexp_results > 0) {
                        comment_text.setText(Html.fromHtml(String.format("%s...", text.substring(0, 500))));
                        comment_text.setAutoLinkMask(0);
                    } else {
                        comment_text.setText(String.format("%s...", text.substring(0, 500)));
                    }
                    expand_text_btn.setVisibility(View.VISIBLE);
                    final int finalRegexp_results = regexp_results;
                    final String finalText = text;
                    expand_text_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (finalRegexp_results > 0) {
                                comment_text.setText(Html.fromHtml(finalText));
                                comment_text.setAutoLinkMask(0);
                            } else {
                                comment_text.setText(finalText);
                            }
                            expand_text_btn.setVisibility(View.GONE);
                        }
                    });
                } else {
                    if (regexp_results > 0) {
                        comment_text.setText(Html.fromHtml(text));
                        comment_text.setAutoLinkMask(0);
                    } else {
                        comment_text.setText(text);
                    }
                }
                comment_text.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                comment_text.setText("");
            }
            if (item.avatar != null) {
                author_avatar.setImageBitmap(item.avatar);
            }
            if (position == getItemCount() - 1) {
                divider.setVisibility(View.GONE);
            } else {
                divider.setVisibility(View.VISIBLE);
            }
            comment_photo.setVisibility(View.GONE);
            try {
                for (int i = 0; i < item.attachments.size(); i++) {
                    if (item.attachments.get(i).type.equals("photo")
                            && item.attachments.get(i).status.equals("done")) {
                        if (item.attachments.get(i) != null) {
                            comment_photo.setImageBitmap(((Photo) item.attachments.get(i)).bitmap);
                            comment_photo.setVisibility(View.VISIBLE);
                            comment_photo.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    viewPhotoAttachment(item);
                                }
                            });
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        public void viewPhotoAttachment(Comment comment) {
            WallPost item;
            Intent intent = new Intent(ctx.getApplicationContext(), PhotoViewerActivity.class);
            intent.putExtra("where", "comments");
            try {
                intent.putExtra("local_photo_addr",
                        String.format("%s/comment_photos/comment_photo_o%sp%s",
                                ctx.getCacheDir(),
                                comment.author_id, comment.id));
                if(comment.attachments != null) {
                    for(int i = 0; i < comment.attachments.size(); i++) {
                        if(comment.attachments.get(i).type.equals("photo")) {
                            Photo photo = ((Photo) comment.attachments.get(i));
                            intent.putExtra("original_link", photo.original_url);
                            intent.putExtra("author_id", comment.author_id);
                            intent.putExtra("photo_id", photo.id);
                        }
                    }
                }
                ctx.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void setArray(ArrayList<Comment> array) {
        items = array;
    }
}