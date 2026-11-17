/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import jnr.constants.platform.Errno;
import jnr.ffi.Platform;
import net.openhft.posix.OpenFlag;
import net.openhft.posix.PosixAPI;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class UnixContractTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();
    private PosixAPI api;

    @Before
    public void unixProvider() {
        assumeTrue(Platform.getNativePlatform().isUnix());
        api = new JNRPosixAPI();
    }

    @Test
    public void createPreservesExistingContents() throws Exception {
        File file = temporary.newFile();
        byte[] contents = {1, 2, 3, 4};
        Files.write(file.toPath(), contents);
        int fd = api.open(file.toString(), OpenFlag.O_RDWR.value() | OpenFlag.O_CREAT.value(), 0600);
        assertTrue(api.lastErrorStr(), fd >= 0);
        try {
            assertArrayEquals(contents, Files.readAllBytes(file.toPath()));
        } finally {
            assertEquals(0, api.close(fd));
        }
    }

    @Test
    public void createMissingFileAndRejectExclusiveReplacement() throws Exception {
        File file = new File(temporary.getRoot(), "missing file");
        int flags = OpenFlag.O_RDWR.value() | OpenFlag.O_CREAT.value() | OpenFlag.O_EXCL.value();
        int fd = api.open(file.toString(), flags, 0600);
        assertTrue(api.lastErrorStr(), fd >= 0);
        assertEquals(0, api.close(fd));
        assertTrue(file.isFile());
        assertEquals(-1, api.open(file.toString(), flags, 0600));
        assertEquals(Errno.EEXIST.intValue(), api.lastError());
    }

    @Test
    public void appendIgnoresSeekAndTruncateIsExplicit() throws Exception {
        File file = temporary.newFile();
        Files.write(file.toPath(), new byte[]{1, 2});
        int fd = api.open(file.toString(), OpenFlag.O_WRONLY.value() | OpenFlag.O_APPEND.value(), 0600);
        assertTrue(api.lastErrorStr(), fd >= 0);
        long buffer = api.malloc(1);
        try {
            UNSAFE.putByte(buffer, (byte) 3);
            assertEquals(0, api.lseek(fd, 0, 0));
            assertEquals(1, api.write(fd, buffer, 1));
            assertArrayEquals(new byte[]{1, 2, 3}, Files.readAllBytes(file.toPath()));
        } finally {
            api.free(buffer);
            assertEquals(0, api.close(fd));
        }
        fd = api.open(file.toString(), OpenFlag.O_WRONLY.value() | OpenFlag.O_TRUNC.value(), 0600);
        assertTrue(api.lastErrorStr(), fd >= 0);
        try {
            assertEquals(0, file.length());
        } finally {
            assertEquals(0, api.close(fd));
        }
    }

    @Test
    public void mappingFailureReturnsStatusAndErrno() {
        assertEquals(-1, api.mmap(0, 4096, 1, 1, -1, 0));
        assertEquals(Errno.EBADF.intValue(), api.lastError());
    }

    @Test
    public void lockingInvalidRangeReturnsFalse() {
        if (Platform.getNativePlatform().getOS() == Platform.OS.LINUX) {
            assertFalse(api.mlock(0, 1));
            assertNotEquals(0, api.lastError());
        }
        // Darwin can successfully wire its reserved PAGEZERO region. An address
        // range that wraps is invalid on both systems, independent of mappings.
        assertFalse(api.mlock(-1L, 2));
        assertNotEquals(0, api.lastError());
        assertFalse(api.mlock2(-1L, 2, true));
        assertNotEquals(0, api.lastError());
        if (Platform.getNativePlatform().getOS() != Platform.OS.LINUX)
            assertEquals(Errno.ENOSYS.intValue(), api.lastError());
    }

    @Test
    public void invalidAffinityReturnsStatusAndErrno() {
        assumeTrue(Platform.getNativePlatform().getOS() == Platform.OS.LINUX);
        assertEquals(-1, api.sched_getaffinity(0, 0, 0));
        assertEquals(Errno.EINVAL.intValue(), api.lastError());
        assertEquals(-1, api.sched_setaffinity(0, 0, 0));
        assertEquals(Errno.EINVAL.intValue(), api.lastError());
    }

    @Test(expected = java.io.IOException.class)
    public void diskUsageMissingFileIsAnIoFailure() throws Exception {
        api.du(new File(temporary.getRoot(), "missing file").toString());
    }
}
