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

/**
 * This error is thrown when the {@link HttpClient} failed to execute a network
 * operation.
 * @author Pixmob
 */
public class HttpClientException extends Exception {
    private static final long serialVersionUID = 1L;

    HttpClientException(final String message, final Throwable cause) {
        super(message);
        initCause(cause);
    }

    HttpClientException(final String message) {
        this(message, null);
    }
}
