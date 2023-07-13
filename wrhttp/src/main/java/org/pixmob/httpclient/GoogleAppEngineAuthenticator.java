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

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * This {@link HttpRequestHandler} implementation authenticates requests to a
 * remote GAE application.
 * <p>
 * Using this class requires the permission
 * <tt>android.permission.USE_CREDENTIALS</tt>.
 * </p>
 * @author Pixmob
 */
public class GoogleAppEngineAuthenticator extends AbstractAccountAuthenticator {
    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private final String gaeHost;
    private String authCookieValue;

    /**
     * Create a new instance of this authenticator.
     * @param context
     *            application context
     * @param account
     *            Google account
     * @param gaeHost
     *            Google App Engine hostname, such as <tt>myapp.appspot.com</tt>
     */
    public GoogleAppEngineAuthenticator(final Context context, final Account account, final String gaeHost) {
        super(context, account);
        if (gaeHost == null) {
            throw new IllegalArgumentException("Google App Engine host is required");
        }
        this.gaeHost = gaeHost;
    }

    private String fetchAuthCookie(String authToken, boolean invalidateToken) throws HttpClientException {
        final AccountManager am = (AccountManager) getContext().getSystemService(Context.ACCOUNT_SERVICE);
        if (invalidateToken) {
            // Invalidate authentication token, and generate a new one.
            am.invalidateAuthToken(GOOGLE_ACCOUNT_TYPE, authToken);
            authToken = generateAuthToken();
        }

        final String loginUrl = "https://" + gaeHost + "/_ah/login?continue=http://localhost/&auth="
                + urlEncode(authToken);
        final HttpClient hc = new HttpClient(getContext());
        final HttpResponse resp;
        try {
            resp = hc.get(loginUrl).expect(HttpURLConnection.HTTP_MOVED_TEMP).execute();
        } catch (HttpClientException e) {
            throw new HttpClientException("Authentication failed", e);
        }

        // The authentication was successful.
        // Now we need to get the authentication cookie from the response.

        final Map<String, String> cookies = resp.getCookies();
        String authCookie = cookies.get("SACSID");
        if (authCookie == null) {
            if (!invalidateToken) {
                // Try again with a new authentication token.
                return fetchAuthCookie(authToken, true);
            }
        }
        if (authCookie == null) {
            throw new HttpClientException("Authentication failed");
        }

        return authCookie;
    }

    private static String urlEncode(String str) {
        String encoded = str;
        try {
            encoded = URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Unlikely to happen.
        }
        return encoded;
    }

    private String generateAuthToken() throws HttpClientException {
        return generateAuthToken("ah");
    }

    @Override
    public void onRequest(HttpURLConnection conn) throws Exception {
        if (authCookieValue == null) {
            final String authToken = generateAuthToken();
            final String authCookie = fetchAuthCookie(authToken, false);
            authCookieValue = "SACSID=" + authCookie;
        }

        // The authentication cookie is only stored in memory, in order to
        // prevent security issues.

        conn.addRequestProperty("Cookie", authCookieValue);
    }
}
