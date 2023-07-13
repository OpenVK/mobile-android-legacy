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

import static org.pixmob.httpclient.Constants.HTTP_DELETE;
import static org.pixmob.httpclient.Constants.HTTP_GET;
import static org.pixmob.httpclient.Constants.HTTP_HEAD;
import static org.pixmob.httpclient.Constants.HTTP_POST;
import static org.pixmob.httpclient.Constants.HTTP_PUT;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Build;

/**
 * The Http client is responsible for sending Http requests.
 * @author Pixmob
 */
public final class HttpClient {
    static {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {
            // Disable connection pooling before Froyo:
            // http://stackoverflow.com/a/4261005/422906
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static final String DEFAULT_USER_AGENT = getDefaultUserAgent();
    private final Context context;
    private int connectTimeout;
    private int readTimeout;
    private String userAgent;
    private final Map<String, String> inMemoryCookies = new HashMap<String, String>(8);

    /**
     * Create a new instance for this {@link Context}.
     */
    public HttpClient(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
    }

    Context getContext() {
        return context;
    }

    /**
     * Prepare a new request with the request method <code>GET</code>.
     */
    public HttpRequestBuilder get(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return new HttpRequestBuilder(this, uri, HTTP_GET);
    }

    /**
     * Prepare a new request with the request method <code>HEAD</code>.
     */
    public HttpRequestBuilder head(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return new HttpRequestBuilder(this, uri, HTTP_HEAD);
    }

    /**
     * Prepare a new request with the request method <code>POST</code>.
     */
    public HttpRequestBuilder post(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return new HttpRequestBuilder(this, uri, HTTP_POST);
    }

    /**
     * Prepare a new request with the request method <code>PUT</code>.
     */
    public HttpRequestBuilder put(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return new HttpRequestBuilder(this, uri, HTTP_PUT);
    }

    /**
     * Prepare a new request with the request method <code>DELETE</code>.
     */
    public HttpRequestBuilder delete(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        return new HttpRequestBuilder(this, uri, HTTP_DELETE);
    }

    /**
     * Get the connect timeout in seconds.
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * Set the connect timeout in seconds.
     */
    public void setConnectTimeout(int connectTimeout) {
        if (connectTimeout < 0) {
            throw new IllegalArgumentException("Invalid connect timeout: " + connectTimeout);
        }
        this.connectTimeout = connectTimeout;
    }

    /**
     * Get the read timeout in seconds.
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * Set the read timeout in seconds.
     */
    public void setReadTimeout(int readTimeout) {
        if (readTimeout < 0) {
            throw new IllegalArgumentException("Invalid read timeout: " + readTimeout);
        }
        this.readTimeout = readTimeout;
    }

    /**
     * Get the user agent sent with every request.
     */
    public String getUserAgent() {
        if (userAgent == null) {
            return DEFAULT_USER_AGENT;
        }
        return userAgent;
    }

    /**
     * Set the user agent sent with every request.
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    Map<String, String> getInMemoryCookies() {
        return inMemoryCookies;
    }

    /**
     * Get the default Http User Agent for this client.
     */
    private static final String getDefaultUserAgent() {
        return "PixmobHttpClient (" + Build.MANUFACTURER + " " + Build.MODEL + "; Android "
                + Build.VERSION.RELEASE + "/" + Build.VERSION.SDK_INT + ")";
    }
}
