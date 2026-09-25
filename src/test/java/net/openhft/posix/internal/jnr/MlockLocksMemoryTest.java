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
import static org.junit.Assert.assertTrue;
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
    /** VmLck reports KiB while the native wrapper accepts bytes. */
    private static final int BYTES_PER_KIB = 1024;

    private static final long LOCK_LEN = 4096;

    @Test
    public void mlockReportedSuccessImpliesMemoryActuallyLocked() throws IOException {
        assumeTrue("enable with -Dposix.mlock.integration=true",
                Boolean.getBoolean("posix.mlock.integration"));
        assumeTrue("needs Linux /proc/self/status", new File("/proc/self/status").exists());
        final PosixAPI posix = PosixAPI.posix();
        assumeTrue("needs the JNR provider", posix instanceof JNRPosixAPI);
        final JNRPosixAPI jnr = (JNRPosixAPI) posix;

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
                System.out.printf("mlock evidence: result=%s "
                                + "VmLck before=%d KiB after=%d KiB "
                                + "requested=%d bytes%n",
                        locked, lockedBefore, lockedAfter, LOCK_LEN);

                if (locked) {
                    // The honest contract: a true result means the pages are genuinely locked.
                    long requestedKb = LOCK_LEN / BYTES_PER_KIB;
                    assertTrue("mlock did not lock the requested length",
                            lockedAfter - lockedBefore >= requestedKb);
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

    /** Locked memory in KiB from {@code /proc/self/status} ({@code VmLck}). */
    private static long vmLckKb() throws IOException {
        for (String line : Files.readAllLines(new File("/proc/self/status").toPath()))
            if (line.startsWith("VmLck:"))
                return Long.parseLong(line.replaceAll("[^0-9]", ""));
        throw new IOException("VmLck is absent from /proc/self/status");
    }
}
