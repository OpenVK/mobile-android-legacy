package uk.openvk.android.legacy.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Arrays;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.activities.AppActivity;
import uk.openvk.android.legacy.api.enumerations.HandlerMessages;

public class ErrorLayout extends LinearLayout{
    public ErrorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.error_layout, null);

        this.addView(view);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = RelativeLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        ((TextView) findViewById(R.id.reason_text)).setVisibility(GONE);
    }
    
    public void setReason(int message) {
        String description = "";
        if(message == HandlerMessages.NO_INTERNET_CONNECTION) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(0));
        } else if(message == HandlerMessages.INVALID_JSON_RESPONSE) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(1));
        } else if(message == HandlerMessages.CONNECTION_TIMEOUT) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(2));
        } else if(message == HandlerMessages.BROKEN_SSL_CONNECTION) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(3));
        } else if(message == HandlerMessages.INTERNAL_ERROR) {
            description = getResources().getString(R.string.reason, Arrays.asList(getResources().getStringArray(R.array.connection_error_reasons)).get(4));
        }
        if(description.length() > 0) {
            ((TextView) findViewById(R.id.reason_text)).setVisibility(VISIBLE);
            ((TextView) findViewById(R.id.reason_text)).setText(description);
        } else {
            ((TextView) findViewById(R.id.reason_text)).setVisibility(GONE);
        }
    }

    public void setRetryAction(final Context ctx, final String method, final String args) {
        ((TextView) findViewById(R.id.retry_btn)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ctx.getClass().getSimpleName().equals("AppActivity")) {
                    ((AppActivity) ctx).retryConnection(method, args);
                }
            }
        });
    }
}
