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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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
    static { Log.setLevel(Level.INFO); }

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setupStream() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        var builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        var appenderBuilder = builder.newAppender("Stdout", "CONSOLE")
            .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%msg%n%throwable"));
        builder.add(appenderBuilder);

        builder.add(builder.newLogger("StdOut", Level.INFO)
                .add(builder.newAppenderRef("Stdout"))
                .addAttribute("additivity", false));

        appenderBuilder = builder.newAppender("Stderr", "CONSOLE")
            .addAttribute("target", ConsoleAppender.Target.SYSTEM_ERR);
        appenderBuilder.add(builder.newLayout("PatternLayout")
                .addAttribute("pattern", "%msg%n%throwable"));
        builder.add(appenderBuilder);

        builder.add(builder.newLogger("StdErr", Level.INFO)
                .add(builder.newAppenderRef("Stderr"))
                .addAttribute("additivity", false));

        Configurator.initialize(builder.build());
    }

    @AfterEach
    public void teardown() {
        System.setOut(originalOut);
        outContent.reset();
        System.setErr(originalErr);
        errContent.reset();
    }

    @Test
    public void testLogBufFlush() {
        var logBuf = LogBuf.create(DstSet.values());
        try (var buf = new EventLog<>(logBuf)) {
            buf.to(DstSet.OUT, Log.ERROR).add("hello1").add("hello2");
            buf.to(DstSet.ERR, Log.ERROR).add("err1");
            buf.to(DstSet.OUT_ERR, Log.ERROR).add("outerr");
        }

        try (var buf = new EventLog<>(logBuf)) {
            buf.to(DstSet.OUT, Log.ERROR).add("hello3");
        }

        assertEquals("hello1hello2\nouterr\nhello3\n", outContent.toString());
        assertEquals("err1\nouterr\n", errContent.toString());

        assertTrue(Log.INFO.isEnabled());
        outContent.reset();
        try (var buf = new EventLog<>(logBuf)) {
            buf.to(DstSet.OUT, Log.INFO).add("info-msg");
        }
        assertEquals("info-msg\n", outContent.toString());

        assertFalse(Log.DEBUG.isEnabled());
        outContent.reset();
        try (var buf = new EventLog<>(logBuf)) {
            buf.to(DstSet.OUT, Log.DEBUG).add("debug-msg");
        }
        assertEquals("", outContent.toString());
    }

    enum Dst implements LogDst {
        OUT("StdOut"),
        ERR("StdErr");

        private final Logger logger;

        Dst(String loggerKey) {
            this.logger = LogManager.getLogger(loggerKey);
        }

        @Override
        public Logger getLogger() {
            return logger;
        }
    }

    enum DstSet implements LogDstSet<Dst> {
        OUT(Dst.OUT),
        ERR(Dst.ERR),
        OUT_ERR(Dst.OUT, Dst.ERR);

        private final Dst[] set;

        DstSet(Dst... dsts) {
            this.set = dsts;
        }

        @Override
        public Dst[] set() {
            return set;
        }
    }
}
