/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal;

import org.junit.Test;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NativeLoaderInterruptTest {
    @Test(timeout = 10000)
    public void interruptDuringLoadingDoesNotCloseExtractionChannel() throws IOException {
        Path binary = Files.createTempFile("posix-native-load", ".bin");
        Thread caller = Thread.currentThread();
        try (FileChannel channel = FileChannel.open(binary, StandardOpenOption.READ)) {
            PosixAPIHolder.runWithoutInterruption(() -> {
                // Arrive after the loader started, as an event-loop shutdown can do.
                caller.interrupt();
                try {
                    assertEquals(0L, channel.size());
                } catch (IOException e) {
                    throw new AssertionError("Interrupt closed the native extraction channel", e);
                }
            });
            assertTrue("Loading consumed the caller's interrupt", caller.isInterrupted());
        } finally {
            Thread.interrupted();
            Files.deleteIfExists(binary);
        }
    }

    @Test(timeout = 10000)
    public void loaderFailurePreservesItsCauseAndCallerInterrupt() {
        Thread caller = Thread.currentThread();
        IllegalArgumentException failure = new IllegalArgumentException("unknown provider");
        try {
            PosixAPIHolder.runWithoutInterruption(() -> {
                caller.interrupt();
                throw failure;
            });
            fail("Provider failure was swallowed");
        } catch (IllegalArgumentException expected) {
            assertSame(failure, expected);
            assertTrue("Failure consumed the caller's interrupt", caller.isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }
}
