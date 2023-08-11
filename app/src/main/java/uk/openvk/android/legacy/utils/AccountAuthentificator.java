package uk.openvk.android.legacy.utils;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import uk.openvk.android.legacy.ui.core.activities.AuthActivity;

/**
 * OPENVK LEGACY LICENSE NOTIFICATION
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program. If not, see https://www.gnu.org/licenses/.
 * <p>
 * Source code: https://github.com/openvk/mobile-android-legacy
 */

public class AccountAuthentificator extends AbstractAccountAuthenticator {

    private final Context ctx;

    public AccountAuthentificator(Context ctx) {
        super(ctx);
        this.ctx = ctx;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse,
                             String s, String s2, String[] strings, Bundle bundle)
            throws NetworkErrorException {
        Intent i = new Intent(this.ctx, AuthActivity.class);
        i.putExtra("accountAuthenticatorResponse", accountAuthenticatorResponse);
        bundle.putParcelable("intent", i);
        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                     Account account, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse,
                               Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String s) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse,
                                    Account account, String s, Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse,
                              Account account, String[] strings) throws NetworkErrorException {
        return null;
    }
}
