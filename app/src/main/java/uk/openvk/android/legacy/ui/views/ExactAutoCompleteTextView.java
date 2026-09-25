package uk.openvk.android.legacy.ui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

@SuppressLint("AppCompatCustomView")
public class ExactAutoCompleteTextView extends AutoCompleteTextView {

    public ExactAutoCompleteTextView(Context context) {
        super(context);
    }

    public ExactAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExactAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean enoughToFilter() {
        boolean exactMatching = false;
        boolean startsWith = false;

        if(getAdapter() != null) {

            String text = getText().toString();

            for(int i = 0; i < getAdapter().getCount(); i++) {
                String itemText = getAdapter().getItem(i).toString();

                if(itemText.equals(text)) {
                    exactMatching = true;
                    break;
                }

                if(itemText.startsWith(text)) {
                    startsWith = true;
                    break;
                }
            }
        }

        return !exactMatching && startsWith && super.enoughToFilter();
    }
}
