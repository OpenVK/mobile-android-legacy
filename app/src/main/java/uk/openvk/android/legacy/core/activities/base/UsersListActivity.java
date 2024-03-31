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

package uk.openvk.android.legacy.core.activities.base;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;

import uk.openvk.android.legacy.R;
import uk.openvk.android.client.entities.User;
import uk.openvk.android.legacy.core.fragments.base.UsersFragment;
import uk.openvk.android.legacy.ui.views.ErrorLayout;
import uk.openvk.android.legacy.ui.views.ProgressLayout;

public class UsersListActivity extends NetworkFragmentActivity {

    private ProgressLayout progressLayout;
    private ErrorLayout errorLayout;
    private UsersFragment usersFragment;
    private FragmentTransaction ft;
    public FrameLayout frameLayout;
    public Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent);
        installFragments();
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void installFragments() {
        frameLayout = findViewById(R.id.app_fragment);
        frameLayout.setVisibility(View.GONE);
        progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        errorLayout = (ErrorLayout) findViewById(R.id.error_layout);
        usersFragment = new UsersFragment();
        usersFragment.setActivityContext(this);
        progressLayout.setVisibility(View.VISIBLE);
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.app_fragment, usersFragment, "users");
        ft.commit();
    }

    public void disableProgressBar() {
        progressLayout.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
    }

    public void showProfile(long user_id) {
        String url = "openvk://ovk/id" + user_id;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setPackage(getPackageName());
        startActivity(i);
    }

    public void createAdapter(ArrayList<User> users) {
        usersFragment.createAdapter(this, users);
    }
}
