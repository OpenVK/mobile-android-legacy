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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} wrapper which prevents closing.
 * @author Pixmob
 */
class UncloseableInputStream extends InputStream {
    private static final InputStream NULL_STREAM = new ByteArrayInputStream(new byte[0]);
    private final InputStream delegate;

    public UncloseableInputStream() {
        this(NULL_STREAM);
    }

    public UncloseableInputStream(final InputStream delegate) {
        this.delegate = delegate;
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

    @Override
    public int read() throws IOException {
        return delegate.read();
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        return delegate.read(buffer, offset, length);
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return delegate.read(buffer);
    }

    @Override
    public void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return delegate.skip(byteCount);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public void close() throws IOException {
        // Do not close the stream.
    }

    public void forceClose() {
        IOUtils.close(delegate);
    }
}
