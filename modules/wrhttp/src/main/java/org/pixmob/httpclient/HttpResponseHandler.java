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
 * This handler is notified when an Http response is received.
 * @author Pixmob
 */
public class HttpResponseHandler {
    /**
     * The request failed to connect or read data in time.
     */
    public void onTimeout() throws Exception {
        throw new HttpClientException("Timeout");
    }
    
    /**
     * The request was successfully sent and a response is available.
     */
    public void onResponse(HttpResponse response) throws Exception {
    }
}
