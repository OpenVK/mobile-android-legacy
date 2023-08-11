package uk.openvk.android.legacy.ui.view.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import uk.openvk.android.legacy.R;

/** OPENVK LEGACY LICENSE NOTIFICATION
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

public class MessagesDateSeparator extends LinearLayout {
    public MessagesDateSeparator(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.separator_messages_date, null);
        this.addView(view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        layoutParams.setMargins((int)(8 * getResources().getDisplayMetrics().scaledDensity),
                (int)(4 * getResources().getDisplayMetrics().scaledDensity),
                (int)(4 * getResources().getDisplayMetrics().scaledDensity), (int)(4 *
                        getResources().getDisplayMetrics().scaledDensity));
        ((TextView) view.findViewById(R.id.date_text)).setLayoutParams(layoutParams);
    }
}
