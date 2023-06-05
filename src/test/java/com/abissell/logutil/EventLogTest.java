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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.EnumSet;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EventLogTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setupStream() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void teardown() {
        System.setOut(originalOut);
        outContent.reset();
    }

    @Test
    public void testLogBufFlush() {
        var logBuf = LogBuf.create(DstSet.class);
        try (var buf = new EventLog<>(logBuf)) {
            buf.to(DstSet.FIRST, Log.ERROR).add("hello1").add("hello2");
        }

        try (var buf = new EventLog<>(logBuf)) {
            buf.to(DstSet.FIRST, Log.ERROR).add("hello3");
        }

        assertEquals("hello1hello2\nhello3\n", outContent.toString());
    }

    enum Dst implements LogDst {
        FIRST;

        private final Logger logger;

        Dst() {
            var builder = ConfigurationBuilderFactory.newConfigurationBuilder();
            var appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
                    .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
            appenderBuilder.add(builder.newLayout("PatternLayout")
                    .addAttribute("pattern", "%msg%n%throwable"));
            builder.add(appenderBuilder);

            builder.add(builder.newLogger("StdOut", Level.INFO)
                    .add(builder.newAppenderRef("Stdout"))
                    .addAttribute("additivity", false));

            Configurator.initialize(builder.build());

            this.logger = LogManager.getLogger("StdOut");
        }

        @Override
        public Logger getLogger() {
            return logger;
        }
    }

    enum DstSet implements LogDstSet<Dst> {
        FIRST(Dst.FIRST);

        private final EnumSet<Dst> set;

        DstSet(Dst... dsts) {
            this.set = EnumSet.of(dsts[0], dsts);
        }

        @Override
        public EnumSet<Dst> set() {
            return set;
        }
    }
}
