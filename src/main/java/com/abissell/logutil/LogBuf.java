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

import java.util.EnumMap;

public record LogBuf<S extends Enum<S> & LogDstSet<?>>(
        EnumMap<Log, OptBuf>[] bufs, S[] allSets
) {
    public static <S extends Enum<S> & LogDstSet<?>>
    LogBuf<S> create(S[] allSets) {
        @SuppressWarnings("unchecked")
        EnumMap<Log, OptBuf>[] bufs = (EnumMap<Log, OptBuf>[]) new EnumMap[allSets.length];
        for (S set : allSets) {
            bufs[set.ordinal()] = new EnumMap<>(Log.class);
            for (Log log : Log.values()) {
                if (log.isEnabled()) {
                    bufs[set.ordinal()].put(log, new OptBuf.Buf(new StringBuilder()));
                } else {
                    bufs[set.ordinal()].put(log, OptBuf.NOOP);
                }
            }
        }
        return new LogBuf<>(bufs, allSets);
    }

    public OptBuf to(S dstSet, Log log) {
        return getBuf(dstSet, log);
    }

    private OptBuf getBuf(S dstSet, Log log) {
        return bufs[dstSet.ordinal()].get(log);
    }

    void flush() {
        for (S dstSet : allSets) {
            for (Log log : Log.ENABLED_LEVELS.orElseThrow()) {
                OptBuf.Buf buf = (OptBuf.Buf) bufs[dstSet.ordinal()].get(log);
                if (buf.length() > 0) {
                    var str = buf.getAndClear();
                    for (LogDst dst : dstSet.set()) {
                        log.to(dst, str);
                    }
                }
            }
        }
    }
}
