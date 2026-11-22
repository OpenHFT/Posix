/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import net.openhft.posix.OpenFlag;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class WinJNRPosixAPITest {
    private WinJNRPosixAPI api;

    @Before
    public void windowsOnly() {
        assumeTrue(jnr.ffi.Platform.getNativePlatform().getOS() == jnr.ffi.Platform.OS.WINDOWS);
        api = new WinJNRPosixAPI();
    }

    @Test
    public void binaryRoundTrip() throws Exception {
        byte[] bytes = {0, 10, 13, 10, 26, (byte) 255};
        Path file = Files.createTempFile("posix-binary", ".dat");
        long buffer = api.malloc(bytes.length);
        int fd = api.open(file.toString(), OpenFlag.O_RDWR.value(), 0600);
        try {
            assertTrue(fd >= 0);
            for (int i = 0; i < bytes.length; i++)
                UNSAFE.putByte(buffer + i, bytes[i]);
            assertEquals(bytes.length, api.write(fd, buffer, bytes.length));
            assertArrayEquals(bytes, Files.readAllBytes(file));
            assertEquals(0, api.lseek(fd, 0, 0));
            UNSAFE.setMemory(buffer, bytes.length, (byte) 0);
            assertEquals(bytes.length, api.read(fd, buffer, bytes.length));
            for (int i = 0; i < bytes.length; i++)
                assertEquals(bytes[i], UNSAFE.getByte(buffer + i));
            assertEquals(0, api.read(fd, buffer, bytes.length));
            assertEquals(0, api.write(fd, buffer, 0));
        } finally {
            if (fd >= 0) api.close(fd);
            api.free(buffer);
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void seekBeyondFourGiB() throws Exception {
        Path file = Files.createTempFile("posix-seek", ".dat");
        int fd = api.open(file.toString(), OpenFlag.O_RDWR.value(), 0600);
        try {
            assertTrue(fd >= 0);
            long offset = (1L << 32) + 123;
            assertEquals(offset, api.lseek(fd, offset, 0));
            assertEquals(offset, api.lseek(fd, 0, 1));
            assertEquals(0, Files.size(file));
        } finally {
            if (fd >= 0) api.close(fd);
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void truncateChangesLengthAndPreservesPosition() throws Exception {
        Path file = Files.createTempFile("posix-truncate", ".dat");
        int fd = api.open(file.toString(), OpenFlag.O_RDWR.value(), 0600);
        try {
            assertTrue(fd >= 0);
            assertEquals(7, api.lseek(fd, 7, 0));
            assertEquals(0, api.ftruncate(fd, 4096));
            assertEquals(4096, Files.size(file));
            assertArrayEquals(new byte[4096], Files.readAllBytes(file));
            assertEquals(7, api.lseek(fd, 0, 1));
            assertEquals(0, api.ftruncate(fd, 3));
            assertEquals(3, Files.size(file));
            assertEquals(7, api.lseek(fd, 0, 1));
            assertEquals(-1, api.ftruncate(fd, -1));
            assertEquals(22, api.lastError());
            assertEquals(3, Files.size(file));
        } finally {
            if (fd >= 0) api.close(fd);
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void invalidDescriptorReportsCrtError() {
        assertEquals(-1, api.ftruncate(-1, 1));
        assertEquals(9, api.lastError());
        assertEquals(-1, api.lseek(-1, 0, 0));
        assertEquals(9, api.lastError());
    }

    @Test
    public void readWriteErrorsAndNegativeLengths() {
        long buffer = api.malloc(1);
        try {
            assertEquals(-1, api.read(-1, buffer, 1));
            assertEquals(9, api.lastError());
            assertEquals(-1, api.write(-1, buffer, 1));
            assertEquals(9, api.lastError());
            assertEquals(-1, api.read(-1, buffer, -1));
            assertEquals(22, api.lastError());
            assertEquals(-1, api.write(-1, buffer, -1));
            assertEquals(22, api.lastError());
        } finally {
            api.free(buffer);
        }
    }

    @Test
    public void truncateReadOnlyFileFails() throws Exception {
        Path file = Files.createTempFile("posix-readonly", ".dat");
        Files.write(file, new byte[] {1, 2, 3});
        int fd = api.open(file.toString(), OpenFlag.O_RDONLY.value(), 0);
        try {
            assertTrue(fd >= 0);
            assertEquals(-1, api.ftruncate(fd, 0));
            assertTrue(api.lastError() != 0);
            assertEquals(3, Files.size(file));
        } finally {
            if (fd >= 0) api.close(fd);
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void errnoIsIsolatedBetweenThreads() throws Exception {
        assertEquals(-1, api.ftruncate(-1, 1));
        assertEquals(9, api.lastError());
        FutureTask<Integer> otherError = new FutureTask<>(() -> {
            assertEquals(-1, api.ftruncate(-1, -1));
            return api.lastError();
        });
        Thread thread = new Thread(otherError);
        thread.start();
        assertEquals(Integer.valueOf(22), otherError.get());
        thread.join();
        assertEquals(9, api.lastError());
    }
}
