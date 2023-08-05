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

/**
 * This {@link HttpRequestHandler} implementation adds Http Basic Authentication
 * to requests.
 * @author Pixmob
 */
public class HttpBasicAuthenticator extends HttpRequestHandler {
    private final String authHeaderValue;

    public HttpBasicAuthenticator(final String user, final String password) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password is required");
        }

        // Compute authorization header value.
        final String rawAuthToken = user + ":" + password;
        final byte[] rawAuthTokenBytes;
        try {
            rawAuthTokenBytes = rawAuthToken.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to encode user credentials", e);
        }

        // Source code for Android's Base64.java is included in this library,
        // because this class is not available in Eclair.

        final String authToken = Base64.encodeToString(rawAuthTokenBytes, Base64.DEFAULT);
        authHeaderValue = "Basic " + authToken;
    }

    @Override
    public void onRequest(HttpURLConnection conn) throws Exception {
        // Http Basic Authentication is broken in Froyo/Eclair:
        // http://code.google.com/p/android/issues/detail?id=9579#c6.
        // The authentication header value must be set manually.
        conn.setRequestProperty("Authorization", authHeaderValue);
    }
}
