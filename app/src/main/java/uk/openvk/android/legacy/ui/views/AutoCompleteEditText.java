/*
 *  Copyleft © 2022-24, 2026 OpenVK Team
 *  Copyleft © 2022-24, 2026 Dmitry Tretyakov (aka. Tinelix)
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
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.nineoldandroids.animation.ObjectAnimator;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.list.items.InstancesListItem;

public class AutoCompleteEditText extends LinearLayout {
    private CharSequence text;
    private int dropdownLayoutRes;
    private BaseAdapter adapter;
    int usableWidth;
    boolean isShownDropDown;

    public AutoCompleteEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view =  LayoutInflater.from(getContext()).inflate(
                R.layout.layout_autocomplete, null
        );

        this.addView(view);
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.width = LayoutParams.MATCH_PARENT;
        layoutParams.height = LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.AutoCompleteEditText, 0, 0
        );

        try {
            String text = a.getString(R.styleable.AutoCompleteEditText_text);
            String hint = a.getString(R.styleable.AutoCompleteEditText_textHint);

            dropdownLayoutRes = a.getResourceId(
                    R.styleable.AutoCompleteEditText_dropdownLayout,
                    android.R.layout.simple_dropdown_item_1line
            );

            setText(text);
            setHint(hint);

            int completionThreshold = a.getInt(
                    R.styleable.AutoCompleteEditText_completionThreshold,
                    1
            );

            setThreshold(completionThreshold);
        } finally {
            a.recycle();
        }

        findViewById(R.id.actionButton).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AutoCompleteTextView tv = findViewById(R.id.editText);

                if(!isShownDropDown) {
                    tv.showDropDown();
                    isShownDropDown = true;
                } else {
                    tv.dismissDropDown();
                    isShownDropDown = false;
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    float[] fArr = new float[2];
                    fArr[0] = isShownDropDown ? 0 : -180;
                    fArr[1] = isShownDropDown ? -180 : 0;
                    ObjectAnimator.ofFloat(view, "rotation", fArr).setDuration(300L).start();
                } else {
                    RotateAnimation anim = new RotateAnimation(
                            isShownDropDown ? 0 : -180, isShownDropDown ? -180 : 0,
                            1, 0.5f, 1, 0.5f
                    );
                    anim.setFillAfter(true);
                    anim.setDuration(300L);
                    view.startAnimation(anim);
                }
            }
        });

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);

                usableWidth = getWidth();
                ((AutoCompleteTextView) findViewById(R.id.editText)).setDropDownWidth(usableWidth);
                ((AutoCompleteTextView) findViewById(R.id.editText)).setDropDownAnchor(getId());
            }
        });
    }

    public void setAdapter(ArrayAdapter adapter) {
        this.adapter = adapter;
        ((AutoCompleteTextView) findViewById(R.id.editText)).setAdapter(adapter);
    }

    public void setText(CharSequence text) {
        ((AutoCompleteTextView) findViewById(R.id.editText)).setText(text);
    }

    public void setHint(CharSequence text) {
        ((AutoCompleteTextView) findViewById(R.id.editText)).setHint(text);
    }

    public String getText() {
        return ((AutoCompleteTextView) findViewById(R.id.editText)).getText().toString();
    }

    public void setInputType(int type) {
        ((AutoCompleteTextView) findViewById(R.id.editText)).setInputType(type);
    }

    public void setThreshold(int threshold) {
        ((AutoCompleteTextView) findViewById(R.id.editText)).setThreshold(threshold);
    }

    public ListAdapter getAdapter() {
        return ((AutoCompleteTextView) findViewById(R.id.editText)).getAdapter();
    }

    public void hideDropDown() {
        ((AutoCompleteTextView) findViewById(R.id.editText)).dismissDropDown();
    }
}
