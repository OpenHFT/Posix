/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import net.openhft.posix.internal.windows.WindowsNative;
import net.openhft.posix.internal.windows.WindowsPosixAPI;

import org.junit.Test;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class WindowsRetainedHandleTest {
    @Test
    public void closeWaitsForForceAndClosesOnlyTheDuplicate() throws Exception {
        assumeTrue(
                System.getProperty("os.name").startsWith("Windows") && UNSAFE.addressSize() == 8);
        CountDownLatch forcing = new CountDownLatch(1);
        CountDownLatch proceed = new CountDownLatch(1);
        AtomicInteger closes = new AtomicInteger();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        WindowsNative binding =
                (WindowsNative)
                        Proxy.newProxyInstance(
                                WindowsNative.class.getClassLoader(),
                                new Class<?>[] {WindowsNative.class},
                                (proxy, method, args) -> {
                                    switch (method.getName()) {
                                        case "GetCurrentProcess":
                                            return -1L;
                                        case "DuplicateHandle":
                                            assertEquals(100L, args[1]);
                                            UNSAFE.putLong((long) args[3], 200L);
                                            return 1;
                                        case "FlushFileBuffers":
                                            assertEquals(200L, args[0]);
                                            forcing.countDown();
                                            assertTrue(proceed.await(5, TimeUnit.SECONDS));
                                            assertEquals(0, closes.get());
                                            return 1;
                                        case "CloseHandle":
                                            assertEquals(200L, args[0]);
                                            closes.incrementAndGet();
                                            return 1;
                                        default:
                                            throw new AssertionError(method.getName());
                                    }
                                });
        FileDescriptor descriptor = new FileDescriptor();
        UNSAFE.putLong(
                descriptor,
                UNSAFE.objectFieldOffset(FileDescriptor.class.getDeclaredField("handle")),
                100L);
        PosixAPI.FileSync retained = new WindowsPosixAPI(binding).retainFileSync(descriptor);
        Thread force =
                new Thread(
                        () -> {
                            try {
                                retained.force();
                            } catch (Throwable t) {
                                failure.compareAndSet(null, t);
                            }
                        },
                        "retained-handle-force");
        Thread close =
                new Thread(
                        () -> {
                            try {
                                retained.close();
                            } catch (Throwable t) {
                                failure.compareAndSet(null, t);
                            }
                        },
                        "retained-handle-close");
        force.start();
        try {
            assertTrue(forcing.await(5, TimeUnit.SECONDS));
            close.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (close.getState() != Thread.State.BLOCKED
                    && close.isAlive()
                    && System.nanoTime() < deadline) Thread.yield();
            assertEquals(
                    "Close must wait for in-flight force", Thread.State.BLOCKED, close.getState());
            assertEquals(0, closes.get());
        } finally {
            proceed.countDown();
            force.join(5000);
            close.join(5000);
        }
        assertFalse(force.isAlive());
        assertFalse(close.isAlive());
        assertNull(failure.get());
        retained.close();
        assertEquals("Close is idempotent", 1, closes.get());
        assertThrows(IOException.class, retained::force);
        assertEquals(
                100L,
                UNSAFE.getLong(
                        descriptor,
                        UNSAFE.objectFieldOffset(FileDescriptor.class.getDeclaredField("handle"))));
    }
}
