/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import net.openhft.posix.MMapFlag;
import net.openhft.posix.MMapProt;
import net.openhft.posix.OpenFlag;
import net.openhft.posix.PosixAPI;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Regression test for Posix#15: {@code mlock} must not report success without actually locking memory.
 * <p>
 * The previous implementation returned {@code true} on Azul JVMs without calling {@code mlock} at all,
 * and (on every vendor) compared the native return value directly with {@code ENOMEM} instead of reading
 * {@code errno} after a {@code -1}. This test proves the reported outcome matches reality by reading the
 * process's locked-memory count ({@code VmLck} in {@code /proc/self/status}) before and after the call.
 */
public class MlockLocksMemoryTest {

    private static final long LOCK_LEN = 4096;

    @Test
    public void mlockReportedSuccessImpliesMemoryActuallyLocked() throws IOException {
        assumeTrue("needs Linux /proc/self/status", new File("/proc/self/status").exists());
        final JNRPosixAPI jnr = (JNRPosixAPI) PosixAPI.posix();

        final Path file = Files.createTempFile("mlock", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
        try {
            assertEquals(0, jnr.ftruncate(fd, 1L << 16));
            final long addr = jnr.mmap(0, 1L << 16, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
            assertNotEquals(-1, addr);
            try {
                final long lockedBefore = vmLckKb();
                final boolean locked = jnr.mlock(addr, LOCK_LEN);
                final long lockedAfter = vmLckKb();

                if (locked) {
                    // The honest contract: a true result means the pages are genuinely locked.
                    assertNotEquals("mlock returned true but VmLck did not increase (false success)",
                            lockedBefore, lockedAfter);
                } else {
                    // A false result (e.g. RLIMIT_MEMLOCK exhausted) must not have locked anything.
                    assertEquals("mlock returned false but VmLck increased", lockedBefore, lockedAfter);
                    assumeTrue("environment cannot lock memory (RLIMIT_MEMLOCK); nothing to prove", false);
                }
            } finally {
                jnr.munmap(addr, 1L << 16);
            }
        } finally {
            jnr.close(fd);
            Files.deleteIfExists(file);
        }
    }

    /** Locked memory in KiB from {@code /proc/self/status} ({@code VmLck}), or 0 if absent. */
    private static long vmLckKb() throws IOException {
        for (String line : Files.readAllLines(new File("/proc/self/status").toPath()))
            if (line.startsWith("VmLck:"))
                return Long.parseLong(line.replaceAll("[^0-9]", ""));
        return 0;
    }
}
