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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.http.conn.ssl.BrowserCompatHostnameVerifier;

import android.content.Context;
import android.os.Build;
import android.util.Log;

/**
 * This class is used to prepare and execute an Http request.
 * @author Pixmob
 */
public final class HttpRequestBuilder {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CONTENT_CHARSET = "UTF-8";
    private static final Map<String, List<String>> NO_HEADERS = new HashMap<String, List<String>>(0);
    private static TrustManager[] trustManagers;
    private final byte[] buffer = new byte[1024];
    private final HttpClient hc;
    private final List<HttpRequestHandler> reqHandlers = new ArrayList<HttpRequestHandler>(2);
    private String uri;
    private String method;
    private Set<Integer> expectedStatusCodes = new HashSet<Integer>(2);
    private Map<String, String> cookies;
    private Map<String, List<String>> headers;
    private Map<String, String> parameters;
    private byte[] content;
    private boolean contentSet;
    private String contentType;
    private String streamContentType;
    private HttpResponseHandler handler;
    private InputStream contentStream;
    private String contentDisposition;
    private HttpProgressHandler progressHandler;
    private SSLSocketFactory ssf;

    HttpRequestBuilder(final HttpClient hc, final String uri, final String method) {
        this.hc = hc;
        this.uri = uri;
        this.method = method;
    }

    public HttpRequestBuilder with(HttpRequestHandler handler) {
        if (handler != null) {
            reqHandlers.add(handler);
        }
        return this;
    }

    public HttpRequestBuilder withProgressHandler(HttpProgressHandler handler) {
        if(handler != null) {
            progressHandler = new HttpProgressHandler();
        }
        return this;
    }

    public HttpRequestBuilder expect(int... statusCodes) {
        if (statusCodes != null) {
            for (final int statusCode : statusCodes) {
                if (statusCode < 1) {
                    throw new IllegalArgumentException("Invalid status code: " + statusCode);
                }
                expectedStatusCodes.add(statusCode);
            }
        }
        return this;
    }

    public HttpRequestBuilder content(byte[] content, String contentType) {
        this.content = content;
        this.contentType = contentType;
        if (content != null) {
            contentSet = true;
        }
        return this;
    }

    public HttpRequestBuilder content(InputStream in, String contentType) {
        this.contentStream = in;
        this.streamContentType = contentType;
        if (in != null) {
            contentSet = true;
        }
        return this;
    }

    public HttpRequestBuilder cookies(Map<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }

    public HttpRequestBuilder headers(Map<String, List<String>> headers) {
        this.headers = headers;
        return this;
    }

    public HttpRequestBuilder header(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Header name cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Header value cannot be null");
        }
        if (headers == null) {
            headers = new HashMap<String, List<String>>(2);
        }
        List<String> values = headers.get(name);
        if (values == null) {
            values = new ArrayList<String>(1);
            headers.put(name, values);
        }
        values.add(value);
        return this;
    }

    public HttpRequestBuilder params(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public HttpRequestBuilder param(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Parameter value cannot be null");
        }
        if (parameters == null) {
            parameters = new HashMap<String, String>(4);
        }
        parameters.put(name, value);
        return this;
    }

    public HttpRequestBuilder cookie(String name, String value) {
        if (name == null) {
            throw new IllegalArgumentException("Cookie name cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Cookie value cannot be null");
        }
        if (cookies == null) {
            cookies = new HashMap<String, String>(2);
        }
        cookies.put(name, value);
        return this;
    }

    public HttpRequestBuilder to(HttpResponseHandler handler) {
        this.handler = handler;
        return this;
    }

    public HttpRequestBuilder to(File file) throws IOException {
        to(new WriteToOutputStreamHandler(new FileOutputStream(file)));
        return this;
    }

    public HttpRequestBuilder to(OutputStream output) {
        to(new WriteToOutputStreamHandler(output));
        return this;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "StatementWithEmptyBody"})
    public HttpResponse execute() throws HttpClientException {
        HttpURLConnection conn = null;
        UncloseableInputStream payloadStream = null;
        try {
            if (parameters != null && !parameters.isEmpty()) {
                final StringBuilder buf = new StringBuilder(256);
                if (HTTP_GET.equals(method) || HTTP_HEAD.equals(method)) {
                    buf.append('?');
                }

                int paramIdx = 0;
                for (final Map.Entry<String, String> e : parameters.entrySet()) {
                    if (paramIdx != 0) {
                        buf.append("&");
                    }
                    final String name = e.getKey();
                    final String value = e.getValue();
                    buf.append(URLEncoder.encode(name, CONTENT_CHARSET)).append("=")
                            .append(URLEncoder.encode(value, CONTENT_CHARSET));
                    ++paramIdx;
                }

                if (!contentSet
                        && (HTTP_POST.equals(method) || HTTP_DELETE.equals(method) || HTTP_PUT.equals(method))) {
                    try {
                        content = buf.toString().getBytes(CONTENT_CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        // Unlikely to happen.
                        throw new HttpClientException("Encoding error", e);
                    }
                } else {
                    uri += buf;
                }
            }

            boolean isHttps = uri.startsWith("https://");

            if(hc.getProxy() == null) {
                conn = (HttpURLConnection) new URL(uri).openConnection();
            } else {
                conn = (HttpURLConnection) new URL(uri).openConnection(hc.getProxy());
            }
            conn.setConnectTimeout(hc.getConnectTimeout());
            conn.setReadTimeout(hc.getReadTimeout());
            conn.setAllowUserInteraction(false);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(method);
            conn.setUseCaches(false);
            conn.setDoInput(true);

            if (headers != null && !headers.isEmpty()) {
                for (final Map.Entry<String, List<String>> e : headers.entrySet()) {
                    final List<String> values = e.getValue();
                    if (values != null) {
                        final String name = e.getKey();
                        for (final String value : values) {
                            conn.addRequestProperty(name, value);
                        }
                    }
                }
            }

            if (cookies != null && !cookies.isEmpty() || hc.getInMemoryCookies() != null
                    && !hc.getInMemoryCookies().isEmpty()) {
                final StringBuilder cookieHeaderValue = new StringBuilder(256);
                prepareCookieHeader(cookies, cookieHeaderValue);
                prepareCookieHeader(hc.getInMemoryCookies(), cookieHeaderValue);
                conn.setRequestProperty("Cookie", cookieHeaderValue.toString());
            }

            final String userAgent = hc.getUserAgent();
            if (userAgent != null) {
                conn.setRequestProperty("User-Agent", userAgent);
            }

            conn.setRequestProperty("Connection", "close");
            conn.setRequestProperty("Location", uri);
            conn.setRequestProperty("Referrer", uri);
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestProperty("Accept-Charset", CONTENT_CHARSET);

            if (conn instanceof HttpsURLConnection) {
                if(ssf != null) {
                    setupSecureConnection(ssf, (HttpsURLConnection) conn);
                } else {
                    setupSecureConnection(hc.getContext(), (HttpsURLConnection) conn);
                }
            }

            if (HTTP_POST.equals(method) || HTTP_DELETE.equals(method) || HTTP_PUT.equals(method)) {
                if (content != null) {
                    conn.setDoOutput(true);
                    if (!contentSet) {
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset="
                                + CONTENT_CHARSET);
                    } else if (contentType != null) {
                        conn.setRequestProperty("Content-Type", contentType);
                    }
                    conn.setFixedLengthStreamingMode(content.length);

                    final OutputStream out = conn.getOutputStream();
                    out.write(content, 0, content.length);
                    out.flush();
                } else if (contentStream != null) {
                    conn.setDoOutput(true);
                    if (!contentSet) {
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset="
                                + CONTENT_CHARSET);
                    } else if (contentType != null) {
                        conn.setRequestProperty("Content-Type", contentType);
                    }
                    String body_header = "";
                    if(contentDisposition != null && contentDisposition.length() > 0) {
                        if (streamContentType != null && streamContentType.length() > 0) {
                            body_header = String.format(
                                    "--*****\r\n" +
                                            "Content-Disposition: %s\r\n" +
                                            "Content-Transfer-Encoding: binary\r\n\r\n",
                                    contentDisposition);
                        } else {
                            body_header = String.format(
                                    "--*****\r\n" +
                                            "Content-Disposition: %s\r\n" +
                                            "Content-Type: %s\r\n" +
                                            "Content-Transfer-Encoding: binary\r\n\r\n",
                                    contentDisposition, streamContentType);
                        }
                    }

                    long streamLength = contentStream.available();
                    conn.setFixedLengthStreamingMode(contentStream.available() +
                            body_header.length() + "\r\n--*****--\r\n".length());

                    final OutputStream out = conn.getOutputStream();
                    out.write(body_header.getBytes());
                    int bytes = 0;
                    long upload_filesize = 0;

                    while((bytes = contentStream.read()) != -1) {
                        out.write(bytes);
                        upload_filesize++;
                        if(upload_filesize % 32 == 0) {
                            if(progressHandler != null) {
                                try {
                                    progressHandler.onProgress(upload_filesize,
                                            streamLength);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    out.write("\r\n--*****--\r\n".getBytes());
                    out.flush();
                } else {
                    conn.setFixedLengthStreamingMode(0);
                }
            }

            for (final HttpRequestHandler connHandler : reqHandlers) {
                try {
                    connHandler.onRequest(conn);
                } catch (HttpClientException e) {
                    throw e;
                } catch (Exception e) {
                    throw new HttpClientException("Failed to prepare request to " + uri, e);
                }
            }

            conn.connect();

            final int statusCode = conn.getResponseCode();
            if (statusCode == -1) {
                throw new HttpClientException("Invalid response from " + uri);
            }

            final Map<String, List<String>> headerFields = conn.getHeaderFields();
            final Map<String, String> inMemoryCookies = hc.getInMemoryCookies();
            if (headerFields != null) {
                final List<String> newCookies = headerFields.get("Set-Cookie");
                if (newCookies != null) {
                    for (final String newCookie : newCookies) {
                        final String rawCookie = newCookie.split(";", 2)[0];
                        final int i = rawCookie.indexOf('=');
                        final String name = rawCookie.substring(0, i);
                        final String value = rawCookie.substring(i + 1);
                        inMemoryCookies.put(name, value);
                    }
                }
            }

            if (isStatusCodeError(statusCode)) {
                // Got an error: cannot read input.
                payloadStream = new UncloseableInputStream(getErrorStream(conn));
            } else {
                payloadStream = new UncloseableInputStream(getInputStream(conn));
            }
            final HttpResponse resp = new HttpResponse(statusCode, payloadStream,
                    headerFields == null ? NO_HEADERS : headerFields, inMemoryCookies,
                    conn.getContentLength());
            if (handler != null) {
                try {
                    handler.onResponse(resp);
                } catch (HttpClientException e) {
                    throw e;
                } catch (Exception e) {
                    throw new HttpClientException("Error in response handler", e);
                }
            } else {
                final File temp = File.createTempFile("httpclient-req-", ".cache", hc.getContext().getCacheDir());
                resp.preload(temp);
                temp.delete();
            }
            return resp;
        } catch (SocketTimeoutException e) {
            if (handler != null) {
                try {
                    handler.onTimeout();
                    return null;
                } catch (HttpClientException e2) {
                    throw e2;
                } catch (Exception e2) {
                    throw new HttpClientException("Error in response handler", e2);
                }
            } else {
                throw new HttpClientException("Response timeout from " + uri, e);
            }
        } catch (IOException e) {
            if(e.getMessage().contains("authentication challenge")) {
                throw new HttpClientException("Authorization required in " + uri, e);
            } else {
                throw new HttpClientException("Connection failed to " + uri, e);
            }
        } finally {
            if (conn != null) {
                if (payloadStream != null) {
                    // Fully read Http response:
                    // http://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html
                    try {
                        while (payloadStream.read(buffer) != -1) {

                        }
                    } catch (IOException ignore) {
                    }
                    payloadStream.forceClose();
                }
                conn.disconnect();
            }
        }
    }

    private static boolean isStatusCodeError(int sc) {
        final int i = sc / 100;
        return i == 4 || i == 5;
    }

    private static void prepareCookieHeader(Map<String, String> cookies, StringBuilder headerValue) {
        if (cookies != null) {
            for (final Map.Entry<String, String> e : cookies.entrySet()) {
                if (headerValue.length() != 0) {
                    headerValue.append("; ");
                }
                headerValue.append(e.getKey()).append("=").append(e.getValue());
            }
        }
    }

    /**
     * Open the {@link InputStream} of an Http response. This method supports
     * GZIP and DEFLATE responses.
     */
    private static InputStream getInputStream(HttpURLConnection conn) throws IOException {
        final List<String> contentEncodingValues = conn.getHeaderFields().get("Content-Encoding");
        if (contentEncodingValues != null) {
            for (final String contentEncoding : contentEncodingValues) {
                if (contentEncoding != null) {
                    if (contentEncoding.contains("gzip")) {
                        return new GZIPInputStream(conn.getInputStream());
                    }
                    if (contentEncoding.contains("deflate")) {
                        return new InflaterInputStream(conn.getInputStream(), new Inflater(true));
                    }
                }
            }
        }
        return conn.getInputStream();
    }

    /**
     * Open the error {@link InputStream} of an Http response. This method
     * supports GZIP and DEFLATE responses.
     */
    private static InputStream getErrorStream(HttpURLConnection conn) throws IOException {
        final List<String> contentEncodingValues = conn.getHeaderFields().get("Content-Encoding");
        if (contentEncodingValues != null) {
            for (final String contentEncoding : contentEncodingValues) {
                if (contentEncoding != null) {
                    if (contentEncoding.contains("gzip")) {
                        return new GZIPInputStream(conn.getErrorStream());
                    }
                    if (contentEncoding.contains("deflate")) {
                        return new InflaterInputStream(conn.getErrorStream(), new Inflater(true));
                    }
                }
            }
        }
        return conn.getErrorStream();
    }

    private static KeyStore loadCertificates(Context context) throws IOException {
        try {
            final KeyStore localTrustStore = KeyStore.getInstance("BKS");
            final InputStream in = context.getResources().openRawResource(R.raw.hc_keystore);
            try {
                localTrustStore.load(in, null);
            } finally {
                in.close();
            }

            return localTrustStore;
        } catch (Exception e) {
            final IOException ioe = new IOException("Failed to load SSL certificates");
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Setup SSL connection.
     */
    private static void setupSecureConnection(Context context, HttpsURLConnection conn) throws IOException {
        final SSLContext sslContext;
        try {
            // SSL certificates are provided by the Guardian Project:
            // https://github.com/guardianproject/cacert
            if (trustManagers == null) {
                // Load SSL certificates:
                // http://nelenkov.blogspot.com/2011/12/using-custom-certificate-trust-store-on.html
                // Earlier Android versions do not have updated root CA
                // certificates, resulting in connection errors.
                final KeyStore keyStore = loadCertificates(context);

                final CustomTrustManager customTrustManager = new CustomTrustManager(keyStore);
                trustManagers = new TrustManager[] { customTrustManager };
            }

            // Init SSL connection with custom certificates.
            // The same SecureRandom instance is used for every connection to
            // speed up initialization.
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, SECURE_RANDOM);
        } catch (GeneralSecurityException e) {
            final IOException ioe = new IOException("Failed to initialize SSL engine");
            ioe.initCause(e);
            throw ioe;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Fix slow read:
            // http://code.google.com/p/android/issues/detail?id=13117
            // Prior to ICS, the host name is still resolved even if we already
            // know its IP address, for each connection.
            final SSLSocketFactory delegate = sslContext.getSocketFactory();
            final SSLSocketFactory socketFactory = new SSLSocketFactory() {
                @Override
                public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
                    InetAddress addr = InetAddress.getByName(host);
                    injectHostname(addr, host);
                    return delegate.createSocket(addr, port);
                }

                @Override
                public Socket createSocket(InetAddress host, int port) throws IOException {
                    return delegate.createSocket(host, port);
                }

                @Override
                public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
                        throws IOException, UnknownHostException {
                    return delegate.createSocket(host, port, localHost, localPort);
                }

                @Override
                public Socket createSocket(InetAddress address, int port, InetAddress localAddress,
                                           int localPort) throws IOException {
                    return delegate.createSocket(address, port, localAddress, localPort);
                }

                private void injectHostname(InetAddress address, String host) {
                    try {
                        Field field = InetAddress.class.getDeclaredField("hostName");
                        field.setAccessible(true);
                        field.set(address, host);
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public Socket createSocket(Socket s, String host, int port, boolean autoClose)
                        throws IOException {
                    injectHostname(s.getInetAddress(), host);
                    return delegate.createSocket(s, host, port, autoClose);
                }

                @Override
                public String[] getDefaultCipherSuites() {
                    return delegate.getDefaultCipherSuites();
                }

                @Override
                public String[] getSupportedCipherSuites() {
                    return delegate.getSupportedCipherSuites();
                }
            };
            conn.setSSLSocketFactory(socketFactory);
        } else {
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
        }

        conn.setHostnameVerifier(new BrowserCompatHostnameVerifier());
    }

    /**
     * Setup SSL connection with custom SSLSocketFactory.
     */
    public static void setupSecureConnection(SSLSocketFactory ssf, HttpsURLConnection conn) throws IOException {
        if(ssf != null) {
            conn.setSSLSocketFactory(ssf);
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        }
    }

    public void setupSecureConnection(SSLSocketFactory ssf) throws IOException {
        this.ssf = ssf;
    }

    public void contentDisposition(String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }
}
