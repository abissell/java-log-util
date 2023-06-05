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
package com.abissell;

import java.util.EnumMap;

public record LogBuf<S extends Enum<S> & LogDstSet<?>>(
        EnumMap<S, EnumMap<Log, OptBuf>> bufs
) {
    public static <S extends Enum<S> & LogDstSet<?>>
    LogBuf<S> create(Class<S> clazz) {
        return new LogBuf<>(new EnumMap<>(clazz));
    }

    public OptBuf to(S dstSet, Log log) {
        return getBuf(dstSet, log);
    }

    private OptBuf getBuf(S dstSet, Log log) {
        var bufsByLevel = bufs.computeIfAbsent(dstSet, s -> new EnumMap<>(Log.class));
        return bufsByLevel.computeIfAbsent(log, l -> {
            if (log.isEnabled(dstSet)) {
                return new OptBuf.Buf(new StringBuilder());
            } else {
                return new OptBuf.Noop();
            }
        });
    }

    void flush() {
        bufs.forEach((dstSet, logs) -> {
            logs.forEach((log, optBuf) -> {
                if (optBuf instanceof (OptBuf.Buf buf)) {
                    if (buf.length() > 0) {
                        var str = buf.getAndClear();
                        dstSet.set().forEach(dst -> log.to(dst, str));
                    }
                }
            });
        });
    }
}
