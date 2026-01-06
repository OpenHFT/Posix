/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NoOpConcurrencyTest {

    @Test
    public void lastErrorIsAlwaysZeroAcrossThreads() throws InterruptedException {
        final NoOpPosixAPI api = new NoOpPosixAPI("for concurrency test");
        int threads = 8;
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                start.await();
                for (int i = 0; i < 1000; i++) {
                    assertEquals(0, api.lastError(), "lastError should always be 0");
                }
            } catch (Throwable t) {
                failures.add(t);
            } finally {
                done.countDown();
            }
        };

        for (int i = 0; i < threads; i++) {
            new Thread(task, "NoOpConcurrencyTest-" + i).start();
        }

        start.countDown();
        done.await();

        assertTrue(failures.isEmpty(), "No failures expected, but got " + failures);
    }
}
