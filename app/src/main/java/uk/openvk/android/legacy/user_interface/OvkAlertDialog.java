package uk.openvk.android.legacy.user_interface;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import uk.openvk.android.legacy.R;

public class OvkAlertDialog extends AlertDialog {
    private AlertDialog dialog;

    public OvkAlertDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void build(AlertDialog.Builder builder) {
        dialog = builder.create();
    }

    @Override
    public void show() {
        super.show();
        if(dialog != null) {
            dialog.show();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                LinearLayout parent = (LinearLayout) dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getParent();
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.login_btn);
                ((LinearLayout.LayoutParams) dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getLayoutParams()).height = ((int) (32 * getContext().getResources().getDisplayMetrics().scaledDensity));
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundResource(R.drawable.login_btn);
                dialog.getButton(DialogInterface.BUTTON_NEUTRAL).getLayoutParams().height = ((int) (32 * getContext().getResources().getDisplayMetrics().scaledDensity));
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.login_btn);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).getLayoutParams().height = ((int) (32 * getContext().getResources().getDisplayMetrics().scaledDensity));
                parent.getLayoutParams().height = ((int) (38 * getContext().getResources().getDisplayMetrics().scaledDensity));
                int divierId = getContext().getResources()
                        .getIdentifier("android:id/titleDivider", null, null);
                View divider = dialog.findViewById(divierId);
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.ovk_color_light));
                divider.setVisibility(View.GONE);
                int buttonBarId = getContext().getResources()
                        .getIdentifier("android:id/buttonPanel", null, null);
                View buttonBar = dialog.findViewById(buttonBarId);
                buttonBar.setPadding(
                        (int) (4 * getContext().getResources().getDisplayMetrics().scaledDensity),
                        (int) (4 * getContext().getResources().getDisplayMetrics().scaledDensity),
                        (int) (4 * getContext().getResources().getDisplayMetrics().scaledDensity),
                        (int) (4 * getContext().getResources().getDisplayMetrics().scaledDensity));
                ((LinearLayout.LayoutParams) buttonBar.getLayoutParams()).height = ((int) (46 * getContext().getResources().getDisplayMetrics().scaledDensity));
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void setButton(int whichButton, CharSequence text, final OnClickListener listener) {
        dialog.setButton(whichButton, text, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onClick(dialog, which);
                dismiss();
            }
        });
    }

    @Override
    public void cancel() {
        dialog.cancel();
    }

    @Override
    public Button getButton(int whichButton) {
        return dialog.getButton(whichButton);
    }

    @Override
    public ListView getListView() {
        return dialog.getListView();
    }

    @Override
    public void hide() {
        dialog.hide();
    }

    @Override
    public void setMessage(CharSequence message) {
        dialog.setMessage(message);
    }

    @Override
    public void setIcon(Drawable icon) {
        dialog.setIcon(icon);
    }

    @Override
    public void setIcon(@DrawableRes int resId) {
        dialog.setIcon(resId);
    }

    @Override
    public void setCancelable(boolean flag) {
        dialog.setCancelable(flag);
    }

    @Override
    public void setTitle(CharSequence title) {
        dialog.setTitle(title);
    }

    @Override
    public void setTitle(@StringRes int titleId) {
        dialog.setTitle(titleId);
    }

    @Override
    public void setCustomTitle(View customTitleView) {
        dialog.setCustomTitle(customTitleView);
    }

    @Override
    public boolean isShowing() {
        return dialog.isShowing();
    }

    @Override
    public void setContentView(@NonNull View view) {
        dialog.setContentView(view);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        dialog.setContentView(layoutResID);
    }

    @Override
    public void setContentView(@NonNull View view, @Nullable ViewGroup.LayoutParams params) {
        dialog.setContentView(view, params);
    }

    @Override
    public void setOnDismissListener(@Nullable OnDismissListener listener) {
        dialog.setOnDismissListener(listener);
    }
}
