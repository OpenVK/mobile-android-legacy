package uk.openvk.android.legacy.ui.list.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

import dev.tinelix.retro_pm.MenuItem;
import dev.tinelix.retro_pm.PopupMenu;
import uk.openvk.android.legacy.Global;
import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.OpenVKAPI;
import uk.openvk.android.legacy.api.entities.OvkExpandableText;
import uk.openvk.android.legacy.api.entities.WallPost;
import uk.openvk.android.legacy.ui.core.activities.AppActivity;
import uk.openvk.android.legacy.ui.core.activities.WallPostActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.GroupIntentActivity;
import uk.openvk.android.legacy.ui.core.activities.intents.ProfileIntentActivity;
import uk.openvk.android.legacy.ui.core.fragments.app.NewsfeedFragment;
import uk.openvk.android.legacy.ui.core.fragments.app.ProfileFragment;
import uk.openvk.android.legacy.ui.view.layouts.PostAttachmentsView;
import uk.openvk.android.legacy.ui.view.layouts.WallLayout;

/*  Copyleft © 2022, 2023 OpenVK Team
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

public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.Holder> {

    private final String instance;
    private final boolean isWall;
    private boolean safeViewing;
    private String where;
    private ArrayList<WallPost> items = new ArrayList<>();
    private Context ctx;
    public LruCache memCache;
    private int resize_videoattachviews;
    private int resize_photoattachments;
    private int photo_fail_count;
    private ImageLoaderConfiguration imageLoaderConfig;
    private DisplayImageOptions displayimageOptions;
    private ImageLoader imageLoader;

    public NewsfeedAdapter(Context context, ArrayList<WallPost> posts, boolean isWall) {
        ctx = context;
        items = posts;
        instance = PreferenceManager.getDefaultSharedPreferences(ctx).getString("current_instance", "");
        safeViewing = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean("safeViewing", true);
        this.isWall = isWall;
        this.displayimageOptions =
                new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.ARGB_8888).build();
        this.imageLoaderConfig =
                new ImageLoaderConfiguration.Builder(ctx.getApplicationContext()).
                        defaultDisplayImageOptions(displayimageOptions)
                        .memoryCacheSize(16777216) // 16 MB memory cache
                        .writeDebugLogs()
                        .build();
        if (ImageLoader.getInstance().isInited()) {
            ImageLoader.getInstance().destroy();
        }
        this.imageLoader = ImageLoader.getInstance();
        imageLoader.init(imageLoaderConfig);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(ctx).inflate(R.layout.list_item_newsfeed, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(position);
    }

    @Override
    public void onViewRecycled(Holder holder) {
        super.onViewRecycled(holder);
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
        public final LinearLayout repost_info;
        public final TextView original_poster_name;
        public final TextView original_post_info;
        public final TextView original_post_text;
        public final TextView likes_counter;
        public final TextView reposts_counter;
        public final TextView comments_counter;
        public final View convertView;
        public final ImageView avatar;
        private final TextView error_label;
        private final TextView expand_text_btn;
        private final TextView repost_expand_text_btn;
        private final ImageView api_app_indicator;
        private final ImageView verified_icon;
        private final ImageButton options_btn;
        private final PostAttachmentsView post_attach_container;
        private final PostAttachmentsView repost_attach_container;
        private PopupMenu p_menu;
        private boolean likeAdded = false;
        private boolean likeDeleted = false;

        public Holder(View view) {
            super(view);
            this.convertView = view;
            this.poster_name = view.findViewById(R.id.poster_name_view);
            this.post_info = view.findViewById(R.id.post_info_view);
            this.post_text = view.findViewById(R.id.post_view);
            this.likes_counter = view.findViewById(R.id.post_likes);
            this.reposts_counter = view.findViewById(R.id.post_reposts);
            this.comments_counter = view.findViewById(R.id.post_comments);
            this.avatar = view.findViewById(R.id.author_avatar);
            this.error_label = (convertView.findViewById(R.id.error_label));
            this.repost_info = (convertView.findViewById(R.id.post_retweet_container));
            this.original_poster_name = (convertView.findViewById(R.id.post_retweet_name));
            this.original_post_info = (convertView.findViewById(R.id.post_retweet_time));
            this.original_post_text = (convertView.findViewById(R.id.post_retweet_text));
            this.expand_text_btn = view.findViewById(R.id.expand_text_btn);
            this.repost_expand_text_btn = view.findViewById(R.id.repost_expand_text_btn);
            this.api_app_indicator = view.findViewById(R.id.api_app_indicator);
            this.verified_icon = view.findViewById(R.id.verified_icon);
            this.options_btn = view.findViewById(R.id.post_options_btn);
            this.post_attach_container = view.findViewById(R.id.post_attach_container);
            this.repost_attach_container = view.findViewById(R.id.repost_attach_container);
        }

        void bind(final int position) {
            final WallPost item = getItem(position);

            options_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showPostOptions(view);
                }
            });

            if(item.post_source.type.equals("api")) {
                api_app_indicator.setVisibility(View.VISIBLE);
                switch (item.post_source.platform) {
                    case "android":
                        api_app_indicator.setImageDrawable(ctx.getResources().
                                getDrawable(R.drawable.ic_api_android_app_indicator));
                        break;
                    case "iphone":
                        api_app_indicator.setImageDrawable(ctx.getResources().getDrawable(
                                R.drawable.ic_api_ios_app_indicator));
                        break;
                    case "mobile":
                        api_app_indicator.setImageDrawable(ctx.getResources().getDrawable(
                                R.drawable.ic_api_mobile_indicator));
                        break;
                    default:
                        api_app_indicator.setVisibility(View.GONE);
                        break;
                }
            } else {
                api_app_indicator.setVisibility(View.GONE);
            }
            poster_name.setText(item.name);
            if(item.verified_author) {
                verified_icon.setVisibility(View.VISIBLE);
            } else {
                verified_icon.setVisibility(View.GONE);
            }
            post_info.setText(item.info);
            expand_text_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openWallComments(ctx, position, null);
                }
            });

            if(!item.is_explicit || !safeViewing) {
                if (item.text.length() > 0) {
                    post_text.setVisibility(View.VISIBLE);
                    String text = item.text.replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&amp;", "&")
                            .replaceAll("&quot;", "\"");
                    String[] lines = text.split("\r\n|\r|\n");
                    String text_llines = "";
                    if (lines.length > 8) {
                        for (int line_no = 0; line_no < 8; line_no++) {
                            if (line_no == 7) {
                                if (lines[line_no].length() > 0)
                                    text_llines += String.format("%s...", lines[line_no]);
                            } else if (line_no == 6) {
                                if (lines[line_no + 1].length() == 0) {
                                    text_llines += String.format("%s", lines[line_no]);
                                } else {
                                    text_llines += String.format("%s\r\n", lines[line_no]);
                                }
                            } else {
                                text_llines += String.format("%s\r\n", lines[line_no]);
                            }
                        }
                        post_text.setText(Global.formatLinksAsHtml(text_llines));
                        expand_text_btn.setVisibility(View.VISIBLE);
                    } else {
                        OvkExpandableText expandableText = Global.formatLinksAsHtml(text, 500);
                        post_text.setText(expandableText.sp_text);
                        if (expandableText.expandable) {
                            expand_text_btn.setVisibility(View.VISIBLE);
                        } else {
                            expand_text_btn.setVisibility(View.GONE);
                        }
                    }
                } else {
                    post_text.setVisibility(View.GONE);
                    expand_text_btn.setVisibility(View.GONE);
                }

                if(item.attachments.size() > 0) {
                    post_attach_container.loadAttachments(
                            items,
                            item,
                            imageLoader,
                            item.attachments,
                            position,
                            isWall
                    );
                } else {
                    post_attach_container.setVisibility(View.GONE);
                }

                if (item.repost != null) {
                    repost_info.setVisibility(View.VISIBLE);
                    original_poster_name.setText(item.repost.name);
                    original_post_info.setText(item.repost.time);
                    String repost_text = item.repost.newsfeed_item.text.replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&amp;", "&").replaceAll("&quot;", "\"");
                    if(repost_text.length() > 0) {
                        String[] repost_lines = item.repost.newsfeed_item.text.split("\r\n|\r|\n");
                        if (repost_lines.length > 8 && item.repost.newsfeed_item.text.length() <= 500) {
                            String text_llines = "";
                            for (int line_no = 0; line_no < 8; line_no++) {
                                if (line_no == 7) {
                                    text_llines += String.format("%s...", repost_lines[line_no]);
                                } else {
                                    text_llines += String.format("%s\r\n", repost_lines[line_no]);
                                }
                            }
                            original_post_text.setText(text_llines);
                            repost_expand_text_btn.setVisibility(View.VISIBLE);
                        } else if (repost_text.length() > 500) {
                            original_post_text.setText(String.format("%s...", repost_text.substring(0, 500)));
                            repost_expand_text_btn.setVisibility(View.VISIBLE);
                        } else {
                            original_post_text.setText(repost_text);
                            repost_expand_text_btn.setVisibility(View.GONE);
                        }
                    } else {
                        original_post_text.setVisibility(View.GONE);
                    }
                    if (item.repost.newsfeed_item.attachments.size() > 0) {
                        repost_attach_container.loadAttachments(items,
                                item.repost.newsfeed_item, imageLoader, item.repost.newsfeed_item.attachments,
                                position, isWall);
                    } else {
                        post_attach_container.setVisibility(View.GONE);
                    }
                    repost_info.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openWallRepostComments(ctx, position, view);
                        }
                    });
                } else {
                    repost_info.setVisibility(View.GONE);
                }
            } else {
                error_label.setText(ctx.getResources().getString(R.string.post_load_nsfw));
                error_label.setVisibility(View.VISIBLE);
                post_text.setVisibility(View.GONE);
            }

            if(!item.is_explicit || !safeViewing) {
                error_label.setVisibility(View.GONE);
            }

            likes_counter.setText(String.format("%s", item.counters.likes));
            reposts_counter.setText(String.format("%s", item.counters.reposts));
            comments_counter.setText(String.format("%s", item.counters.comments));

            if(item.counters.isLiked) {
                likes_counter.setSelected(true);
            } else {
                likes_counter.setSelected(false);
            }

            if(item.counters.enabled) {
                likes_counter.setEnabled(true);
                if(item.counters.isLiked && likeAdded) {
                    likes_counter.setText(String.format("%s", item.counters.likes + 1));
                } else if(!item.counters.isLiked && likeDeleted) {
                    likes_counter.setText(String.format("%s", item.counters.likes - 1));
                } else {
                    likes_counter.setText(String.format("%s", item.counters.likes));
                }
            } else {
                likes_counter.setEnabled(false);
            }

            Bitmap author_avatar = item.avatar;
            if(author_avatar != null) {
                avatar.setImageBitmap(author_avatar);
            } else {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            String.format("%s/%s/photos_cache/newsfeed_avatars/avatar_%s",
                                    ctx.getCacheDir(), instance, item.author_id), options);
                    if (bitmap != null) {
                        avatar.setImageBitmap(bitmap);
                    } else {
                        avatar.setImageDrawable(ctx.getResources().getDrawable(R.drawable.photo_loading));
                    }
                } catch (OutOfMemoryError ignored) {

                }
            }

            convertView.findViewById(R.id.poster_ll).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ctx instanceof AppActivity) {
                        String where = "";
                        if(((AppActivity) ctx).selectedFragment instanceof NewsfeedFragment) {
                            where = "newsfeed";
                        } else {
                            where = "profile";
                        }
                        showAuthorPage(ctx, where, position);
                    } else {
                        showAuthorPage(ctx, "profile", position);
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
                        deleteLike(ctx, position, item,"post", view);
                        item.counters.isLiked = false;
                    } else {
                        if(!likeDeleted) {
                            likeAdded = true;
                        }
                        addLike(ctx, position, item,"post", view);
                        item.counters.isLiked = true;
                    }
                    items.set(position, item);
                }
            });

            reposts_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    repost(position);
                }
            });

            comments_counter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openWallComments(ctx, position, view);
                }
            });
        }

        private void showPostOptions(View view) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                android.widget.PopupMenu popupMenu =
                        new android.widget.PopupMenu(view.getContext(), view);
                popupMenu.getMenu().add(ctx.getResources().getString(R.string.report_content));
                popupMenu.show();
            } else {
                p_menu = new PopupMenu(ctx);
                p_menu.setHeaderTitle("");
                p_menu.add(0, ctx.getResources().getString(R.string.report_content));
                p_menu.setOnItemSelectedListener(new PopupMenu.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(MenuItem item) {
                        Toast.makeText
                                (ctx,
                                        ctx.getResources().getString(R.string.not_implemented),
                                        Toast.LENGTH_LONG).show();
                    }
                });
                p_menu.show(view);
            }
        }

        public void repost(int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            final ArrayList<String> functions = new ArrayList<>();
            builder.setTitle(R.string.repost_dlg_title);
            functions.add(ctx.getResources().getString(R.string.repost_own_wall));
            ArrayAdapter<String> adapter =
                    new ArrayAdapter<>(ctx, R.layout.list_item_select_dialog, R.id.text, functions);
            builder.setSingleChoiceItems(adapter, -1, null);
            final AlertDialog dialog = builder.create();
            dialog.show();
            final WallPost finalPost = getItem(position);
            SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            String current_screen = global_prefs.getString("current_screen", "");
            dialog.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(functions.get(position)
                            .equals(ctx.getResources().getString(R.string.repost_own_wall))) {
                        if(ctx instanceof AppActivity) {
                            Global.openRepostDialog(ctx,
                                    ((AppActivity) ctx).ovk_api,
                                    "own_wall", finalPost);
                        } else if(ctx instanceof ProfileIntentActivity) {
                            Global.openRepostDialog(ctx,
                                    ((ProfileIntentActivity) ctx).ovk_api,
                                    "own_wall", finalPost);
                        } else if(ctx instanceof GroupIntentActivity) {
                            Global.openRepostDialog(ctx,
                                    ((GroupIntentActivity) ctx).ovk_api,
                                    "own_wall", finalPost);
                        }
                        dialog.dismiss();
                    }
                }
            });
        }

        public void openWallComments(Context ctx, int position, View view) {
            OpenVKAPI ovk_api = null;
            SharedPreferences global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            } else {
                return;
            }
            if(ovk_api.account != null) {
                WallPost item;
                item = getItem(position);
                Intent intent = new Intent(ctx.getApplicationContext(), WallPostActivity.class);
                if (ctx instanceof AppActivity &&
                        ((AppActivity) ctx).selectedFragment instanceof NewsfeedFragment) {
                    intent.putExtra("where", "newsfeed");
                } else {
                    intent.putExtra("where", "wall");
                }
                try {
                    intent.putExtra("post_id", item.post_id);
                    intent.putExtra("owner_id", item.owner_id);
                    intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                            ovk_api.account.last_name));
                    intent.putExtra("account_id", ovk_api.account.id);
                    intent.putExtra("post_author_id", item.author_id);
                    intent.putExtra("post_author_name", item.name);
                    intent.putExtra("post_json", item.getJSONString());
                    ctx.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        public void addLike(Context ctx, int position, WallPost item, String post, View view) {
            SharedPreferences global_prefs =
                    android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            OpenVKAPI ovk_api = null;
            NewsfeedFragment newsfeedFragment = null;
            WallLayout wallLayout = null;
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
                newsfeedFragment = ((AppActivity) ctx).fragmentHub.newsfeedFragment;
                if (((AppActivity) ctx).selectedFragment instanceof ProfileFragment) {
                    ProfileFragment profileFragment = ((AppActivity) ctx).fragmentHub.profileFragment;
                    if(profileFragment.getView() != null) {
                        wallLayout = profileFragment.getView().findViewById(R.id.wall_layout);
                        if (wallLayout != null) {
                            wallLayout.select(position, "likes", "add");
                        } else {
                            return;
                        }
                    }
                } else {
                    if(newsfeedFragment != null) {
                        newsfeedFragment.select(position, "likes", "add");
                    } else {
                        return;
                    }
                }
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
                ProfileFragment profileFragment = ((ProfileIntentActivity) ctx).profileFragment;
                if(profileFragment.getView() != null) {
                    wallLayout = (profileFragment.getView().findViewById(R.id.wall_layout));
                } else {
                    return;
                }
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
                wallLayout = ((GroupIntentActivity) ctx).findViewById(R.id.wall_layout);
            } else {
                return;
            }
            ovk_api.likes.add(ovk_api.wrapper, item.owner_id, item.post_id, position);
        }

        public void deleteLike(Context ctx, int position, WallPost item, String post, View view) {
            SharedPreferences global_prefs =
                    android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            OpenVKAPI ovk_api = null;
            NewsfeedFragment newsfeedFragment = null;
            WallLayout wallLayout = null;
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
                newsfeedFragment = ((AppActivity) ctx).fragmentHub.newsfeedFragment;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
                ProfileFragment profileFragment = ((ProfileIntentActivity) ctx).profileFragment;
                if(profileFragment.getView() != null) {
                    wallLayout = (profileFragment.getView().findViewById(R.id.wall_layout));
                } else {
                    return;
                }
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
                wallLayout = ((GroupIntentActivity) ctx).findViewById(R.id.wall_layout);
            } else {
                return;
            }
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
                if(wallLayout != null) {
                    wallLayout.select(0, "likes", "delete");
                } else {
                    return;
                }
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
                if(newsfeedFragment != null) {
                    newsfeedFragment.select(0, "likes", "delete");
                } else {
                    return;
                }
            }
            ovk_api.likes.delete(ovk_api.wrapper, item.owner_id, item.post_id, position);
        }

        public void showAuthorPage(Context ctx, String where, int position) {
            WallPost item;
            SharedPreferences global_prefs =
                    android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            OpenVKAPI ovk_api = null;
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            } else {
                return;
            }

            if (where.equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
            }

            if(item.author_id != ovk_api.account.id) {
                String url = "";
                if (item.author_id < 0) {
                    url = "openvk://group/" + "club" + -item.author_id;
                } else {
                    url = "openvk://profile/" + "id" + item.author_id;
                }
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setPackage("uk.openvk.android.legacy");
                i.setData(Uri.parse(url));
                ctx.startActivity(i);
            } else {
                if(ctx instanceof AppActivity) {
                    ((AppActivity) ctx).openAccountProfile();
                }
            }
        }

        public void openWallRepostComments(Context ctx, int position, View view) {
            WallPost item;
            Intent intent = new Intent(ctx.getApplicationContext(), WallPostActivity.class);
            SharedPreferences global_prefs = null;
            OpenVKAPI ovk_api = null;
            global_prefs = android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
            if(ctx instanceof AppActivity) {
                ovk_api = ((AppActivity) ctx).ovk_api;
            } else if(ctx instanceof ProfileIntentActivity) {
                ovk_api = ((ProfileIntentActivity) ctx).ovk_api;
            } else if(ctx instanceof GroupIntentActivity) {
                ovk_api = ((GroupIntentActivity) ctx).ovk_api;
            } else {
                return;
            }
            if (global_prefs.getString("current_screen", "").equals("profile")) {
                item = ovk_api.wall.getWallItems().get(position);
                intent.putExtra("where", "wall");
            } else {
                item = ovk_api.newsfeed.getWallPosts().get(position);
                intent.putExtra("where", "newsfeed");
            }
            intent.putExtra("where", "wall");
            try {
                intent.putExtra("post_id", item.repost.newsfeed_item.post_id);
                intent.putExtra("owner_id", item.repost.newsfeed_item.owner_id);
                intent.putExtra("account_name", String.format("%s %s", ovk_api.account.first_name,
                        ovk_api.account.last_name));
                intent.putExtra("account_id", ovk_api.account.id);
                intent.putExtra("post_author_id", item.repost.newsfeed_item.author_id);
                intent.putExtra("post_author_name", item.repost.newsfeed_item.name);
                intent.putExtra("post_json", item.repost.newsfeed_item.getJSONString());
                ctx.startActivity(intent);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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