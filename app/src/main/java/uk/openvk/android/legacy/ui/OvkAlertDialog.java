package uk.openvk.android.legacy.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.widget.TextView;

import uk.openvk.android.legacy.R;

/** Copyleft © 2022, 2023 OpenVK Team
 *  Copyleft © 2022, 2023 Dmitry Tretyakov (aka. Tinelix)
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


public class OvkAlertDialog extends AlertDialog {
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private View dlg_view;
    private String title;
    private String type;

    public OvkAlertDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void build(AlertDialog.Builder builder, String title, String message, View view) {
        dlg_view = view;
        this.builder = builder;
        builder.setTitle(title);
        this.title = title;
        builder.setMessage(null);
        if(dlg_view == null) {
            try {
                dlg_view = getLayoutInflater().inflate(R.layout.layout_styled_dialog, null, false);
                TextView message_tv = dlg_view.findViewById(android.R.id.message);
                message_tv.setText(message);
                builder.setView(dlg_view);
            } catch (Exception ignored) {

            }
        }
        dialog = builder.create();
    }

    public void build(AlertDialog.Builder builder, String title, String message, View view, String type) {
        dlg_view = view;
        this.builder = builder;
        builder.setMessage(null);
        if(title.length() > 0) {
            builder.setTitle(title);
        } else {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) builder.setTitle("OpenVK");
        }
        this.title = title;
        this.type = type;
        if(dlg_view == null) {
            try {
                switch (type) {
                    case "progressDlg":
                        dlg_view = getLayoutInflater().inflate(R.layout.dialog_styled_progress, null, false);
                        TextView message_tv = dlg_view.findViewById(android.R.id.message);
                        message_tv.setText(message);
                        builder.setView(dlg_view);
                        builder.setCancelable(false);
                        break;
                    case "listDlg":
                        builder.setView(null);
                        break;
                    default:
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            dlg_view = getLayoutInflater().inflate(R.layout.layout_styled_dialog, null, false);
                            message_tv = dlg_view.findViewById(android.R.id.message);
                            message_tv.setText(message);
                            builder.setView(dlg_view);
                        } else {
                            builder.setMessage(message);
                        }
                        break;
                }
            } catch (Exception ignored) {

            }
        }
        dialog = builder.create();
    }

    @Override
    public void show() {
        super.show();
        if(dialog != null) {
            dialog.show();
            super.dismiss();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && Build.VERSION.SDK_INT <
                    Build.VERSION_CODES.HONEYCOMB) {
                // some style attributes (for example: buttons background, margins, size and etc) in res/values-v7/styles.xml won't changed
                try {
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundResource(R.drawable.login_btn);
                    ((LinearLayout.LayoutParams) dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .getLayoutParams()).height = ((int) (32 * getContext().getResources().getDisplayMetrics().scaledDensity));
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setBackgroundResource(R.drawable.login_btn);
                    ((LinearLayout.LayoutParams) dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .getLayoutParams()).setMargins(
                            ((int) (3 * getContext().getResources().getDisplayMetrics().scaledDensity)),
                            0,
                            ((int) (3 * getContext().getResources().getDisplayMetrics().scaledDensity)),
                            0
                    );
                } catch (Exception ignored) {

                }
                try {
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).getLayoutParams().height =
                            ((int) (32 * getContext().getResources().getDisplayMetrics().scaledDensity));
                    ((LinearLayout.LayoutParams) dialog.getButton(DialogInterface.BUTTON_NEUTRAL).
                            getLayoutParams()).setMargins(
                            ((int) (3 * getContext().getResources().getDisplayMetrics().scaledDensity)),
                            0,
                            ((int) (3 * getContext().getResources().getDisplayMetrics().scaledDensity)),
                            0
                    );
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundResource(R.drawable.login_btn);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).getLayoutParams().height = ((int)
                            (32 * getContext().getResources().getDisplayMetrics().scaledDensity));
                    ((LinearLayout.LayoutParams) dialog.getButton(DialogInterface.BUTTON_POSITIVE).
                            getLayoutParams()).setMargins(
                            ((int) (3 * getContext().getResources().getDisplayMetrics().scaledDensity)),
                            0,
                            ((int) (3 * getContext().getResources().getDisplayMetrics().scaledDensity)),
                            0
                    );
                } catch (Exception ignored) {

                }
                try {
                    LinearLayout parent = (LinearLayout) dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .getParent();
                    parent.getLayoutParams().height = ((int) (38 * getContext().getResources().
                            getDisplayMetrics().scaledDensity));
                    int buttonBarId = getContext().getResources()
                            .getIdentifier("android:id/buttonPanel", null, null);
                    View buttonBar = dialog.findViewById(buttonBarId);
                    buttonBar.setPadding(
                            (int) (6 * getContext().getResources().getDisplayMetrics().scaledDensity),
                            (int) (6 * getContext().getResources().getDisplayMetrics().scaledDensity),
                            (int) (6 * getContext().getResources().getDisplayMetrics().scaledDensity),
                            (int) (6 * getContext().getResources().getDisplayMetrics().scaledDensity));
                    ((LinearLayout.LayoutParams) buttonBar.getLayoutParams()).height = ((int) (50 *
                            getContext().getResources().getDisplayMetrics().scaledDensity));
                } catch (Exception ignored) {

                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

                try {
                    int divierId = getContext().getResources()
                            .getIdentifier("android:id/titleDivider", null, null);
                    View divider = dialog.findViewById(divierId);
                    divider.setBackgroundColor(getContext().getResources().getColor(R.color.ovk_color_light));
                    divider.setVisibility(View.GONE);
                    int titleBarId = getContext().getResources()
                            .getIdentifier("android:id/topPanel", null, null);
                    int customPanelId = getContext().getResources()
                            .getIdentifier("android:id/customPanel", null, null);
                    if (title.length() == 0) {
                        dialog.findViewById(titleBarId).setVisibility(View.GONE);
                        dialog.findViewById(customPanelId).setBackgroundColor(Color.WHITE);
                    }
                } catch (Exception ignored) {

                }
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
        super.cancel();
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

    public void close() {
        dialog.dismiss();
    }

    @Override
    public void setOnShowListener(@Nullable OnShowListener listener) {
        dialog.setOnShowListener(listener);
    }

    public void setProgressText(String message) {
        if(type.equals("progressDlg")) {
            ((TextView) dlg_view.findViewById(android.R.id.message)).setText(message);
        }
    }
}
