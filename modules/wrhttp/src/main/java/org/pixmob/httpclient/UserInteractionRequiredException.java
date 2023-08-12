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

import android.content.Intent;

/**
 * This error is thrown if user interaction is required in order to perform an
 * action.
 * @author Pixmob
 */
public class UserInteractionRequiredException extends HttpClientException {
    private static final long serialVersionUID = 1L;
    private final Intent userIntent;

    public UserInteractionRequiredException(final Intent userIntent) {
        super("User interaction required: please start the intent");
        this.userIntent = userIntent;
    }

    /**
     * If not <code>null</code>, this method returns an {@link Intent} to start
     * an activity. This activity should be started with
     * <code>startActivityForResult</code>, while overriding
     * <code>onActivityResult</code> in an activity or a fragment.
     * @return
     */
    public Intent getUserIntent() {
        return userIntent;
    }
}
