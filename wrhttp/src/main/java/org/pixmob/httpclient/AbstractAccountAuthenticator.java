/*
 * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pixmob.httpclient;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

/**
 * Abstract Http authenticator using {@link AccountManager} to get
 * authentication tokens. Subclass this handler to get easy access to user
 * authentication token from {@link AccountManager}, by calling
 * {@link #generateAuthToken()}. This token allows an application to
 * authenticate using user credential without requesting for the user password.
 * @see #generateAuthToken()
 * @author Pixmob
 */
public abstract class AbstractAccountAuthenticator extends HttpRequestHandler {
    private final Context context;
    private final Account account;

    public AbstractAccountAuthenticator(final Context context, final Account account) {
        if (context == null) {
            throw new IllegalArgumentException("Context is required");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account is required");
        }
        this.context = context;
        this.account = account;
    }

    public final Context getContext() {
        return context;
    }

    public final Account getAccount() {
        return account;
    }

    /**
     * Generate an authentication token. The user must grant credential access
     * when an application is using it for the first time. In this case,
     * {@link UserInteractionRequiredException} is thrown, the
     * {@link UserInteractionRequiredException#getUserIntent()} must be used
     * with <code>startActivityForResult</code> to start a system activity, in
     * order to get access to user credential. The user is free to deny
     * credential access. If credential access is granted, the next call to this
     * method should not throw any error.
     * @see UserInteractionRequiredException#getUserIntent()
     * @throws UserInteractionRequiredException
     *             if user interaction is required in order to perform
     *             authentication
     * @throws HttpClientException
     *             if authentication failed (network error, bad credentials,
     *             etc...)
     */
    protected final String generateAuthToken(String authTokenType) throws HttpClientException {
        // Get an authentication token from the AccountManager:
        // this call is asynchronous, as the user may not respond immediately.
        final AccountManager am = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        final AccountManagerFuture<Bundle> authResultFuture;

        // The AccountManager API for authentication token is different before
        // Ice Cream Sandwich.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            authResultFuture = GetTokenLegacy.INSTANCE.get(am, account, authTokenType);
        } else {
            authResultFuture = GetTokenICS.INSTANCE.get(am, account, authTokenType);
        }

        final Bundle authResult;
        try {
            authResult = authResultFuture.getResult();
        } catch (OperationCanceledException e) {
            throw new HttpClientException("Authentication failed: canceled by user", e);
        } catch (AuthenticatorException e) {
            throw new HttpClientException("Authentication failed", e);
        } catch (IOException e) {
            throw new HttpClientException("Authentication failed: network error", e);
        }
        if (authResult == null) {
            throw new HttpClientException("Authentication failed");
        }

        final String authToken = authResult.getString(AccountManager.KEY_AUTHTOKEN);
        if (authToken == null) {
            // No authentication token found:
            // the user must allow this application to use his account.
            final Intent authPermIntent = (Intent) authResult.get(AccountManager.KEY_INTENT);
            int flags = authPermIntent.getFlags();
            flags &= ~Intent.FLAG_ACTIVITY_NEW_TASK;
            authPermIntent.setFlags(flags);

            // The request is aborted: the application should retry later.
            throw new UserInteractionRequiredException(authPermIntent);
        }
        return authToken;
    }

    private static class GetTokenLegacy {
        public static final GetTokenLegacy INSTANCE = new GetTokenLegacy();

        @SuppressWarnings("deprecation")
        public AccountManagerFuture<Bundle> get(AccountManager am, Account account, String authTokenType) {
            return am.getAuthToken(account, authTokenType, false, null, null);
        }
    }

    private static class GetTokenICS {
        public static final GetTokenICS INSTANCE = new GetTokenICS();

        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        public AccountManagerFuture<Bundle> get(AccountManager am, Account account, String authTokenType) {
            return am.getAuthToken(account, authTokenType, null, false, null, null);
        }
    }
}
