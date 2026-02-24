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

import java.lang.StableValue;
import java.util.function.Supplier;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public enum Log {
    TRACE(Level.TRACE) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.trace(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.trace(msg, throwable);
        }
    },
    DEBUG(Level.DEBUG) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.debug(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.debug(msg, throwable);
        }
    },
    INFO(Level.INFO) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.info(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.info(msg, throwable);
        }
    },
    WARN(Level.WARN) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.warn(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.warn(msg, throwable);
        }
    },
    ERROR(Level.ERROR) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.error(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.error(msg, throwable);
        }
    },
    FATAL(Level.FATAL) {
        @Override
        void toLogger(Logger logger, String msg) {
            logger.fatal(msg);
        }

        @Override
        void toLogger(Logger logger, String msg, Throwable throwable) {
            logger.fatal(msg, throwable);
        }
    };

    private static final StableValue<Level> CONFIGURED_LEVEL = StableValue.of();
    static final StableValue<Log[]> ENABLED_LEVELS = StableValue.of();

    public static void setLevel(Level level) {
        CONFIGURED_LEVEL.setOrThrow(level);

        int enabledCount = 0;
        for (Log logLevel : values()) {
            if (logLevel.isEnabled()) {
                enabledCount++;
            }
        }
        Log[] enabledLevels = new Log[enabledCount];
        int i = 0;
        for (Log logLevel : values()) {
            if (logLevel.isEnabled()) {
                enabledLevels[i++] = logLevel;
            }
        }
        ENABLED_LEVELS.setOrThrow(enabledLevels);
    }

    abstract void toLogger(Logger logger, String msg);
    abstract void toLogger(Logger logger, String msg, Throwable throwable);

    public void to(LogDst dst, String msg) {
        var log = dst.getLogger();
        if (isEnabled()) {
            toLogger(log, msg);
        }
    }

    public void to(LogDst dst, String prefix, String msg) {
        if (isEnabled()) {
            to(dst, prefix + msg);
        }
    }

    public final boolean isEnabled() {
        return level.intLevel() <= CONFIGURED_LEVEL.orElseThrow().intLevel();
    }

    private final Level level;

    Log(Level level) {
        this.level = level;
    }

    public void to(LogDstSet<?> dstSet, String prefix, String msg) {
        if (isEnabled()) {
            var fullMsg = prefix + msg;
            to(dstSet, fullMsg);
        }
    }

    public void to(LogDstSet<?> dstSet, String prefix, Object obj) {
        if (isEnabled()) {
            to(dstSet, prefix, obj.toString());
        }
    }

    public void to(LogDstSet<?> dstSet, String prefix, Supplier<String> msgSupplier) {
        if (isEnabled()) {
            to(dstSet, prefix, msgSupplier.get());
        }
    }

    public void to(LogDstSet<?> dstSet, Supplier<String> msgSupplier) {
        to(dstSet, "", msgSupplier);
    }

    public void to(LogDstSet<?> dstSet, Object obj) {
        if (isEnabled()) {
            to(dstSet, obj.toString());
        }
    }

    public void to(LogDstSet<?> dstSet, String msg) {
        if (isEnabled()) {
            for (LogDst dst : dstSet.set()) {
                to(dst, msg);
            }
        }
    }
}
