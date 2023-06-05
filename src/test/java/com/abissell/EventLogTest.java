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
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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
            buf.to(DstSet.STD_OUT, Log.ERROR).add("hello1").add("hello2");
        }

        try (var buf = new EventLog<>(logBuf)) {
            buf.to(DstSet.STD_OUT, Log.ERROR).add("hello3");
        }

        assertEquals("hello1hello2\nhello3\n", outContent.toString());
    }
}
