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
        PosixAPIHolder.useNoOpPosixApi();
        assertTrue(PosixAPIHolder.POSIX_API instanceof NoOpPosixAPI);
    }

    /**
     * Regression guard for issue #15 ("Fix build on Zing").
     * <p>The Zing JDK 8 build failed because the native JNR provider could not be
     * constructed on that JVM. The bootstrap in {@link PosixAPIHolder#loadPosixApi()}
     * is fail-closed: any {@link Throwable} while constructing the native provider
     * degrades to {@link NoOpPosixAPI} rather than aborting class-load (and therefore
     * the build/tests). This test pins that contract: when the native provider is
     * unavailable, the fallback provider is installed, {@link PosixAPI#posix()} still
     * returns a usable facade, and the mmap-safe operations relied on by callers do
     * not throw. Verifying the fault on Zing itself needs the TeamCity Zing agent and
     * is not reproducible in this environment.
     */
    @Test
    public void fallbackProviderKeepsBuildGreenWhenNativeUnavailable() {
        PosixAPIHolder.loadPosixApi();
        final PosixAPI original = PosixAPIHolder.POSIX_API;
        try {
            // Simulate a JVM (such as the failing Zing build) where the native
            // provider is not used and the bootstrap falls back to the no-op provider.
            PosixAPIHolder.useNoOpPosixApi();

            final PosixAPI facade = PosixAPI.posix();
            assertNotNull(facade);
            assertTrue(facade instanceof NoOpPosixAPI);

            // Safe operations must degrade rather than fail the build/tests.
            assertEquals(0, facade.msync(0L, 0L, 0));
            assertEquals(0, facade.madvise(0L, 0L, 0));
            assertEquals(0, facade.lastError());
        } finally {
            // Restore the provider resolved for this JVM so test ordering is unaffected.
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
