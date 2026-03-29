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

package uk.openvk.android.legacy.ui.preferences;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.list.adapters.AboutAppButtonsAdapter;

public class ButtonGridPreference extends Preference {
    private String target;
    private View view;

    public ButtonGridPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ButtonGridPreference);
        target = a.getString(R.styleable.ButtonGridPreference_target);
        a.recycle();
    }

    public ButtonGridPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        this.view = view;
        linkTarget();
    }

    public void linkTarget() {
        if(target.equals("aboutApp")) {
            AboutAppButtonsAdapter adapter = new AboutAppButtonsAdapter(getContext());
            if(view != null) {
                ((GridView) view).setAdapter(adapter);
                ((GridView) view).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String defInstance = getContext().getResources().getString(R.string.default_instance);
                        String defInstance2 = getContext().getResources().getString(R.string.default_instance_no_https);

                        SharedPreferences global_prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        String instance = global_prefs.getString("current_instance", "");

                        switch (i) {
                            case 0:
                                openWebAddress("https://github.com/openvk/mobile-android-legacy");
                                break;
                            case 1:
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                    openWebAddress(
                                            String.format("https://%s/donate", defInstance)
                                    );
                                } else {
                                    openWebAddress(
                                            String.format("https://%s/donate", defInstance2)
                                    );
                                }
                                break;
                            case 2:
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                    openWebAddress(
                                            String.format("https://%s/topic181_1", defInstance)
                                    );
                                } else {
                                    openWebAddress(
                                            String.format("http://%s/topic181_1", defInstance2)
                                    );
                                }
                                break;
                            case 3:
                                if(instance.equals(defInstance) || instance.equals(defInstance2))
                                    openWebAddress("openvk://group/club181");
                                else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    openWebAddress(String.format("https://%s/app", defInstance));
                                else
                                    openWebAddress(String.format("http://%s/app", defInstance2));
                                break;
                        }
                    }
                });
            }
        }
    }

    private void openWebAddress(String address) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(address));
        getContext().startActivity(i);
    }
}
