/*
 *  Copyleft © 2022, 2023, 2024 OpenVK Team
 *  Copyleft © 2022, 2023, 2024 Dmitry Tretyakov (aka. Tinelix)
 *
 *  This file is part of OpenVK Legacy for Android.
 *
 *  OpenVK Legacy for Android is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU Affero General Public License as published by the Free Software Foundation,
 *  either version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 */

package uk.openvk.android.legacy.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import uk.openvk.android.legacy.R;

public class EditTextAction extends LinearLayout {
    private CharSequence text;

    public EditTextAction(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_editaction, null);

        this.addView(view);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditTextAction, 0, 0);
        try {
            String text = a.getString(R.styleable.EditTextAction_text);
            setText(text);
            String hint = a.getString(R.styleable.EditTextAction_textHint);
            setHint(hint);
        } finally {
            a.recycle();
        }
    }

    public void setActionClickListener(OnClickListener clickListener) {
        ((ImageButton) findViewById(R.id.actionButton)).setOnClickListener(clickListener);
    }

    public void setText(CharSequence text) {
        ((EditText) findViewById(R.id.editText)).setText(text);
    }

    public void setHint(CharSequence text) {
        ((EditText) findViewById(R.id.editText)).setHint(text);
    }

    public String getText() {
        return ((EditText) findViewById(R.id.editText)).getText().toString();
    }
}
