/*
 * Copyright 2023 Andrew Bissell. All Rights Reserved.
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

public sealed interface OptBuf permits OptBuf.Buf, OptBuf.Noop {
    OptBuf add(String str);
    OptBuf add(Object obj);
    OptBuf add(Supplier<String> supplier);
    <T> OptBuf add(Iterator<T> iter, Function<T, String> toStr);
    int length();

    record Buf(StringBuilder buf) implements OptBuf {
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
            buf.delete(0, buf.length());
            return str;
        }
    }

    record Noop() implements OptBuf {
        @Override
        public OptBuf add(String str) {
            return this;
        }

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
}
