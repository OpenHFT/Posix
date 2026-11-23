/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InterruptedProviderLoadTest {
    @Test
    public void firstNativeLoadPreservesAnExistingInterrupt() throws Exception {
        File output = File.createTempFile("posix-interrupted-load", ".log");
        Process child = null;
        try {
            child = new ProcessBuilder(new File(System.getProperty("java.home"), "bin/java").getPath(),
                    "-cp", System.getProperty("java.class.path"),
                    InterruptedProviderLoadTest.class.getName()).redirectErrorStream(true)
                    .redirectOutput(output).start();
            assertTrue("Native loading did not finish", child.waitFor(30, TimeUnit.SECONDS));
            String diagnostic = new String(Files.readAllBytes(output.toPath()), StandardCharsets.UTF_8);
            assertEquals(diagnostic, 0, child.exitValue());
        } finally {
            if (child != null && child.isAlive()) {
                child.destroyForcibly();
                assertTrue("Native loading process survived cleanup", child.waitFor(30, TimeUnit.SECONDS));
            }
            Files.deleteIfExists(output.toPath());
        }
    }

    public static void main(String[] args) {
        Thread.currentThread().interrupt();
        PosixAPI api = PosixAPI.posix();
        assertTrue("Loading consumed the caller's interrupt", Thread.currentThread().isInterrupted());
        assertTrue("Native provider was replaced by a fallback: " + api.getClass(), api.getpid() > 0);
        Thread.interrupted();
    }
}
