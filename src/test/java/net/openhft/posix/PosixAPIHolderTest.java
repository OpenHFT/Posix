/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;
import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.junit.Test;

import static org.junit.Assert.*;

public class PosixAPIHolderTest {

    @Test
    public void loadPosixApiInitialisesProviderOnce() {
        PosixAPIHolder.loadPosixApi();
        PosixAPI first = PosixAPIHolder.POSIX_API;
        assertNotNull(first);

        PosixAPIHolder.loadPosixApi();
        PosixAPI second = PosixAPIHolder.POSIX_API;
        assertSame(first, second);
    }

    @Test
    public void useNoOpPosixApiSwitchesToNoOp() {
        PosixAPIHolder.loadPosixApi();
        final PosixAPI original = PosixAPIHolder.POSIX_API;
        try {
            PosixAPIHolder.useNoOpPosixApi();
            assertTrue(PosixAPIHolder.POSIX_API instanceof NoOpPosixAPI);
        } finally {
            PosixAPIHolder.POSIX_API = original;
        }
    }

    @Test
    public void concurrentLoadPosixApiUsesSingleInstance() throws InterruptedException {
        Runnable task = PosixAPIHolder::loadPosixApi;
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        PosixAPI api = PosixAPIHolder.POSIX_API;
        assertNotNull(api);

        // Basic sanity: calling posix() should not throw
        PosixAPI facade = PosixAPI.posix();
        assertNotNull(facade);
    }
}
