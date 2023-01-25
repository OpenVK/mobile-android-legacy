package uk.openvk.android.legacy.user_interface.list_adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.models.OvkLink;
import uk.openvk.android.legacy.user_interface.activities.ConversationActivity;
import uk.openvk.android.legacy.api.models.Message;
import uk.openvk.android.legacy.user_interface.layouts.IncomingMessageLayout;

public class MessagesListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    ArrayList<Message> objects;
    public boolean opened_sliding_menu;
    public long peer_id;

    public MessagesListAdapter(Context context, ArrayList<Message> items, long peer_id) {
        ctx = context;
        objects = items;
        inflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.peer_id = peer_id;
    }

    @Override
    public int getCount() {
        if(objects != null) {
            return objects.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    Message getMessagesListItem(int position) {
        return ((Message) getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Message item = getMessagesListItem(position);
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.message_item, parent, false);
        }
        Pattern pattern = Pattern.compile("\\[(.+?)\\]|" +
                "((http|https)://)(www.)?[a-zA-Z0-9@:%._\\+~#?&//=]{1,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%._\\+~#?&//=]*)");
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
            if(block.startsWith("[") && block.endsWith("]")) {
                OvkLink link = new OvkLink();
                String[] markup = block.replace("[", "").replace("]", "").split("\\|");
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
                        text = text.replace(block, String.format("<a href=\"%s\">%s</a>", link.url, link.name));
                    }
                }
            } else if(block.startsWith("https://") || block.startsWith("http://")) {
                text = text.replace(block, String.format("<a href=\"%s\">%s</a>", block, block));
            }
            regexp_results = regexp_results + 1;
            regexp_search = matcher.find();
        }

        view.findViewById(R.id.incoming_msg).setVisibility(View.GONE);
        Date startOfDay = new Date(TimeUnit.SECONDS.toMillis(item.timestamp_int));
        startOfDay.setHours(0);
        startOfDay.setMinutes(0);
        startOfDay.setSeconds(0);
        Date prev_startOfDay = null;
        if(position > 0) {
            prev_startOfDay = new Date(TimeUnit.SECONDS.toMillis(getMessagesListItem(position - 1).timestamp_int));
            prev_startOfDay.setHours(0);
            prev_startOfDay.setMinutes(0);
            prev_startOfDay.setSeconds(0);
        }
        if(prev_startOfDay != null) {
            if (startOfDay.compareTo(prev_startOfDay) <= 0) {
                (view.findViewById(R.id.date_separator)).setVisibility(View.GONE);
            } else {
                (view.findViewById(R.id.date_separator)).setVisibility(View.VISIBLE);
                ((TextView) (view.findViewById(R.id.date_separator)).findViewById(R.id.date_text)).setText(new SimpleDateFormat("dd MMMM yyyy").format(startOfDay));
            }
        } else {
            (view.findViewById(R.id.date_separator)).setVisibility(View.VISIBLE);
            ((TextView) (view.findViewById(R.id.date_separator)).findViewById(R.id.date_text)).setText(new SimpleDateFormat("dd MMMM yyyy").format(startOfDay));
        }
        if(item.isIncoming) {
            (view.findViewById(R.id.incoming_msg)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
                        ((ConversationActivity) ctx).getMsgContextMenu(position);
                    }
                }
            });
            (view.findViewById(R.id.incoming_msg)).setVisibility(View.VISIBLE);
            (view.findViewById(R.id.outcoming_msg)).setVisibility(View.GONE);
        } else {
            (view.findViewById(R.id.outcoming_msg)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
                        ((ConversationActivity) ctx).getMsgContextMenu(position);
                    }
                }
            });
            (view.findViewById(R.id.incoming_msg)).setVisibility(View.GONE);
            (view.findViewById(R.id.outcoming_msg)).setVisibility(View.VISIBLE);
        }

        if(item.sending) {
            ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_progress)).setVisibility(View.VISIBLE);
        } else {
            ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_progress)).setVisibility(View.GONE);
            if(item.isError) {
                ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_progress)).setVisibility(View.GONE);
                ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_failed)).setVisibility(View.VISIBLE);
            } else {
                ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_progress)).setVisibility(View.GONE);
                ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_failed)).setVisibility(View.GONE);
            }
        }


        if(item.text.length() > 12) {
            if(item.isIncoming) {
                ((view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.GONE);
                ((view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.VISIBLE);
                try {
                    if (peer_id == item.author_id) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/conversations_avatars/avatar_%s", ctx.getCacheDir(), peer_id), options);
                        ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).setAvatar(bitmap);
                    }
                } catch (OutOfMemoryError error) {

                }
            } else {
                ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.GONE);
                ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.VISIBLE);
            }
        } else {
            if(item.isIncoming) {
                try {
                    if (peer_id == item.author_id) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/conversations_avatars/avatar_%s", ctx.getCacheDir(), peer_id), options);
                        ((IncomingMessageLayout) view.findViewById(R.id.incoming_msg)).setAvatar(bitmap);
                    }
                } catch (OutOfMemoryError error) {

                }
                ((view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.VISIBLE);
                ((view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.GONE);
            } else {
                ((view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_right)).setVisibility(View.VISIBLE);
                (( view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_bottom)).setVisibility(View.GONE);
            }
        }

        if(item.isIncoming) {
            try {
                if (peer_id == item.author_id) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(String.format("%s/photos_cache/conversations_avatars/avatar_%s", ctx.getCacheDir(), peer_id), options);
                }
            } catch (OutOfMemoryError error) {

            }
            if(regexp_results > 0) {
                ((TextView) (view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_text)).setText(Html.fromHtml(text));
                ((TextView) (view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_text)).setAutoLinkMask(0);
            } else {
                ((TextView) (view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_text)).setText(item.text);
            }
            ((TextView) (view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_text)).setText(item.text);
            ((TextView) (view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_right)).setText(item.timestamp);
            ((TextView) (view.findViewById(R.id.incoming_msg)).findViewById(R.id.msg_time_bottom)).setText(item.timestamp);
        } else {
            if(regexp_results > 0) {
                ((TextView) (view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_text)).setText(Html.fromHtml(text));
                ((TextView) (view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_text)).setAutoLinkMask(0);
            } else {
                ((TextView) (view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_text)).setText(item.text);
            }
            ((TextView) (view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_right)).setText(item.timestamp);
            ((TextView) (view.findViewById(R.id.outcoming_msg)).findViewById(R.id.msg_time_bottom)).setText(item.timestamp);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("ConversationActivity")) {
                    ((ConversationActivity) ctx).hideSelectedItemBackground(position);
                }
            }
        });

        return view;
    }

    public void setArray(ArrayList<Message> array) {
        objects = array;
    }

    public class ViewHolder {
        public TextView item_id;
        public TextView item_name;
        public TextView item_avatar;
        public TextView item_online;
    }
}
