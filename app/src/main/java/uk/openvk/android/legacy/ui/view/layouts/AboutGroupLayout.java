package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;

public class AboutGroupLayout extends LinearLayout {

    private String description;
    private String site;

    public AboutGroupLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(getContext()).inflate(
                R.layout.group_about, null);

        this.addView(view);

        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }


    public void setGroupInfo(String description, String site) {
        this.description = description;
        this.site = site;
        if(description != null) {
            if (description.length() > 0) {
                ((TextView) findViewById(R.id.description_label2)).setText(description);
                ((LinearLayout) findViewById(R.id.description_ll)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.description_ll)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.description_ll)).setVisibility(GONE);
        }
        if(site != null) {
            if (site.length() > 0) {
                ((TextView) findViewById(R.id.site_label2)).setText(site);
                ((TextView) findViewById(R.id.site_label2)).setMovementMethod(LinkMovementMethod.getInstance());
                ((LinearLayout) findViewById(R.id.site_ll)).setVisibility(VISIBLE);
            } else {
                ((LinearLayout) findViewById(R.id.site_ll)).setVisibility(GONE);
            }
        } else {
            ((LinearLayout) findViewById(R.id.site_ll)).setVisibility(GONE);
        }
        if(description == null && site == null) {
            findViewById(R.id.about_group_layout).setVisibility(GONE);
        } else if(description.length() == 0 && site.length() == 0) {
            findViewById(R.id.about_group_layout).setVisibility(GONE);
        }
    }

/*  public void setContacts() {

    }
*/
}
