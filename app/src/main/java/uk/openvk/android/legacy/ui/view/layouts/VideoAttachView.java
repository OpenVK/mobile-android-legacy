package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;
import uk.openvk.android.legacy.ui.core.activities.VideoPlayerActivity;

public class VideoAttachView extends FrameLayout {
    private VideoAttachment attachment;
    public VideoAttachView(@NonNull Context context) {
        super(context);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_video, null);

        this.addView(view);
    }

    public VideoAttachView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.attach_video, null);

        this.addView(view);
    }

    public void setAttachment(VideoAttachment attachment) {
        this.attachment = attachment;
        if(attachment != null) {
            ((TextView) findViewById(R.id.attach_title)).setText(attachment.title);
            if (attachment.duration >= 3600) {
                ((TextView) findViewById(R.id.attach_duration)).setText(String.format("%d:%02d:%02d", attachment.duration / 3600, (attachment.duration % 3600) / 60, (attachment.duration % 60)));
            } else {
                ((TextView) findViewById(R.id.attach_duration)).setText(String.format("%d:%02d", attachment.duration / 60, (attachment.duration % 60)));
            }
        }
    }
}
