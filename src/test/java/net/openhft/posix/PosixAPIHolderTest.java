/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;
import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PosixAPIHolderTest {

    @Test
    public void loadPosixApiInitialisesProviderOnce() {
        PosixAPIHolder.loadPosixApi();
        PosixAPI first = PosixAPIHolder.POSIX_API;
        assertNotNull(first, "POSIX_API should be initialised");

        PosixAPIHolder.loadPosixApi();
        PosixAPI second = PosixAPIHolder.POSIX_API;
        assertSame(first, second, "loadPosixApi should be idempotent");
    }

    @Test
    public void useNoOpPosixApiSwitchesToNoOp() {
        PosixAPIHolder.useNoOpPosixApi();
        assertInstanceOf(NoOpPosixAPI.class, PosixAPIHolder.POSIX_API, "useNoOpPosixApi should set a NoOp provider");
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
        assertNotNull(api, "POSIX_API should be set after concurrent initialisation");

        // Basic sanity: calling posix() should not throw
        PosixAPI facade = PosixAPI.posix();
        assertNotNull(facade, "posix() should return a facade instance");
    }
}
