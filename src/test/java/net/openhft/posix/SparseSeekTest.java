/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import jnr.constants.platform.Errno;
import jnr.ffi.Platform;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class SparseSeekTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void distinguishesSparseExtensionFromWrittenData() throws Exception {
        assumeTrue(Platform.getNativePlatform().getOS() == Platform.OS.LINUX);
        File file = temporary.newFile();
        PosixAPI api = new net.openhft.posix.internal.jnr.JNRPosixAPI();
        int fd = api.open(file.toString(), OpenFlag.O_RDWR, 0600);
        assertTrue(api.lastErrorStr(), fd >= 0);
        try {
            assertEquals(0, api.ftruncate(fd, 4096));
            long data = api.lseek(fd, 16, WhenceFlag.SEEK_DATA);
            if (data == -1) {
                int error = api.lastError();
                assumeTrue("Filesystem does not implement SEEK_DATA", error != Errno.EINVAL.intValue());
                assertEquals("A sparse hole must report ENXIO", Errno.ENXIO.intValue(), error);
            } else {
                assertTrue(data >= 16 && data < 4096);
            }
            // Once data is written, an implementation that always returns -1 must fail.
            try (FileOutputStream output = new FileOutputStream(file)) {
                output.write(new byte[4096]);
                output.getFD().sync();
            }
            assertEquals(16, api.lseek(fd, 16, WhenceFlag.SEEK_DATA));
            assertEquals(4095, api.lseek(fd, 4095, WhenceFlag.SEEK_DATA));
            assertEquals(-1, api.lseek(fd, 4096, WhenceFlag.SEEK_DATA));
            assertEquals(Errno.ENXIO.intValue(), api.lastError());
        } finally {
            assertEquals(0, api.close(fd));
        }
    }
}
