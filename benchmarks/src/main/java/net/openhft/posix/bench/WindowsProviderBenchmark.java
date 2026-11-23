/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.bench;

import net.openhft.posix.PosixAPI;
import org.openjdk.jmh.annotations.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
public class WindowsProviderBenchmark {
    @Param({"jnr", "jna", "jna-direct", "ffm"}) public String provider;
    private PosixAPI api;
    private Path file;
    private int fd = -1;
    private long buffer, view;
    @Setup public void setup() throws Exception {
        String type = "unix-jnr".equals(provider) ? "net.openhft.posix.internal.jnr.JNRPosixAPI"
                : "ffm".equals(provider) ? "net.openhft.posix.ffm.WinFFMPosixAPI"
                : "jna-direct".equals(provider) ? "net.openhft.posix.internal.jna.WinJNADirectPosixAPI"
                : "jna".equals(provider) ? "net.openhft.posix.internal.jna.WinJNAPosixAPI"
                : "net.openhft.posix.internal.jnr.WinJNRPosixAPI";
        api = (PosixAPI) Class.forName(type).getDeclaredConstructor().newInstance();
        file = Files.createTempFile("posix-jmh", ".dat");
        fd = api.open(file.toString(), 2, 0600);
        if (fd < 0 || api.ftruncate(fd, 65536) != 0) throw new IllegalStateException(api.lastErrorStr());
        buffer = api.malloc(4096);
        net.openhft.posix.internal.UnsafeMemory.UNSAFE.setMemory(buffer, 4096, (byte) 0);
        view = api.mmap(0, 4096, 3, 1, fd, 0);
        if (view == 0 || view == -1) throw new IllegalStateException(api.lastErrorStr());
    }
    @TearDown public void cleanup() throws Exception {
        if (api != null) {
            if (view != 0 && view != -1) api.munmap(view, 4096);
            if (buffer != 0) api.free(buffer);
            if (fd >= 0) api.close(fd);
        }
        if (file != null) Files.deleteIfExists(file);
    }
    @Benchmark public int threadId() { return api.gettid(); }
    @Benchmark public long realtime() { return api.clock_gettime(); }
    @Benchmark public long smallRead() { api.lseek(fd, 0, 0); return api.read(fd, buffer, 8); }
    @Benchmark public long largeRead() { api.lseek(fd, 0, 0); return api.read(fd, buffer, 4096); }
    @Benchmark public long smallWrite() { api.lseek(fd, 0, 0); return api.write(fd, buffer, 8); }
    @Benchmark public int mapUnmap() {
        long pointer = api.mmap(0, 4096, 3, 1, fd, 0);
        if (pointer == -1) throw new IllegalStateException(api.lastErrorStr());
        return api.munmap(pointer, 4096);
    }
    @Benchmark public int synchronousFlush() { return api.msync(view, 4096, 4); }
}

