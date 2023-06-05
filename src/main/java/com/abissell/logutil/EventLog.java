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

public record EventLog<S extends Enum<S> & LogDstSet<?>>(
        LogBuf<S> buf
) implements AutoCloseable {
    public OptBuf to(S dstSet, Log log) {
        return buf.to(dstSet, log);
    }

    @Override
    public void close() {
        buf.flush();
    }
}
