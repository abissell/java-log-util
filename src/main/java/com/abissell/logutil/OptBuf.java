/*
 * Copyright 2024 Andrew Bissell. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.abissell.logutil;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An {@code OptBuf} is a text buffer which may or may not accumulate inputs
 * into an underlying {@link java.lang.StringBuilder StringBuilder}, intended
 * for use in asynchronous logging of event details at varying logging levels
 * on performance-sensitive code paths. At runtime, if a sufficiently verbose
 * level is enabled for a given log statement, an {@code OptBuf.Buf} can be
 * used, which will append the statement to its internal buffer. Otherwise,
 * {@code OptBuf.NOOP} can be used in its place, in which case the same method
 * calls to add to the {@code OptBuf} are passed to a single statically
 * allocated instance which simply returns itself after a no-op.
 *
 * <p> For example:
 * <pre>{@code
 * var optBuf;
 * if (logger.isEnabled(Level.DEBUG)) {
 *     optBuf = new OptBuf.Buf(new StringBuilder());
 * } else {
 *     optBuf = OptBuf.NOOP;
 * }
 *
 * optBuf.add("some message").add(someObject).add("some trailing message");
 * optBuf.add("another message, interleaved throughout business logic");
 *
 * if (optBuf instanceof OptBuf.Buf buf) {
 *     logger.debug(buf.getAndClear());
 * }
 * }</pre>
 *
 * <p> This can be used to avoid interspersing conditional checks for logging
 * levels in application code, and to accumulate messages to an in-memory
 * buffer for later flushing to persistent storage. Used extensively in
 * {@link com.abissell.logutil.LogBuf LogBuf}, which provides an API for
 * caching {@code OptBuf}s by different output destinations and logging levels.
 *
 * @author Andrew Bissell
 */

public sealed interface OptBuf permits OptBuf.Buf, OptBuf.Noop {

    OptBuf add(boolean b);
    OptBuf add(byte b);
    OptBuf add(char c);
    OptBuf add(short s);
    OptBuf add(int i);
    OptBuf add(long el);
    OptBuf add(float f);
    OptBuf add(double d);
    OptBuf add(CharSequence chars);
    OptBuf add(String str);
    OptBuf add(Object obj);
    OptBuf add(Supplier<String> supplier);
    <T> OptBuf add(Iterator<T> iter, Function<T, String> toStr);
    int length();

    record Buf(StringBuilder buf) implements OptBuf {
        @Override
        public OptBuf add(boolean b) {
            buf.append(b);
            return this;
        }

        @Override
        public OptBuf add(byte b) {
            buf.append(b);
            return this;
        }

        @Override
        public OptBuf add(char c) {
            buf.append(c);
            return this;
        }

        @Override
        public OptBuf add(short s) {
            buf.append(s);
            return this;
        }

        @Override
        public OptBuf add(int i) {
            buf.append(i);
            return this;
        }

        @Override
        public OptBuf add(long el) {
            buf.append(el);
            return this;
        }

        @Override
        public OptBuf add(float f) {
            buf.append(f);
            return this;
        }

        @Override
        public OptBuf add(double d) {
            buf.append(d);
            return this;
        }

        @Override
        public OptBuf add(CharSequence chars) {
            buf.append(chars);
            return this;
        }

        @Override
        public OptBuf add(String str) {
            buf.append(str);
            return this;
        }

        @Override
        public OptBuf add(Object obj) {
            return add(obj.toString());
        }

        @Override
        public OptBuf add(Supplier<String> supplier) {
            return add(supplier.get());
        }

        @Override
        public <T> OptBuf add(Iterator<T> iter, Function<T, String> toStr) {
            iter.forEachRemaining(t -> add(toStr.apply(t)));
            return this;
        }

        @Override
        public int length() {
            return buf.length();
        }

        public String getAndClear() {
            var str = buf.toString();
            buf.setLength(0);
            return str;
        }
    }

    record Noop() implements OptBuf {
        @Override
        public OptBuf add(boolean b) { return this; }

        @Override
        public OptBuf add(byte b) { return this; }

        @Override
        public OptBuf add(char c) { return this; }

        @Override
        public OptBuf add(short s) { return this; }

        @Override
        public OptBuf add(int i) { return this; }

        @Override
        public OptBuf add(long el) { return this; }

        @Override
        public OptBuf add(float f) { return this; }

        @Override
        public OptBuf add(double d) { return this; }

        @Override
        public OptBuf add(CharSequence chars) { return this; }

        @Override
        public OptBuf add(String str) { return this; }

        @Override
        public OptBuf add(Object obj) { return this; }

        @Override
        public OptBuf add(Supplier<String> str) { return this; }

        @Override
        public <T> OptBuf add(Iterator<T> iter, Function<T, String> toStr) { return this; }

        @Override
        public int length() {
            return 0;
        }
    }

    static final Noop NOOP = new Noop();
}
