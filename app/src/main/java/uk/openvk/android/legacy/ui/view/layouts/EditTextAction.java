package uk.openvk.android.legacy.ui.view.layouts;

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
