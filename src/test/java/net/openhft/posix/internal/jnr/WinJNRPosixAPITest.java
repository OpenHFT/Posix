package net.openhft.posix.internal.jnr;

import net.openhft.posix.internal.UnsafeMemory;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class WinJNRPosixAPITest {

    @Test
    public void delegatesToNativeInterfaces() throws Exception {
        RecordingWin win = new RecordingWin();
        RecordingKernel kernel = new RecordingKernel();

        WinJNRPosixAPI api = (WinJNRPosixAPI) UnsafeMemory.UNSAFE.allocateInstance(WinJNRPosixAPI.class);
        setField(api, "jnr", win);
        setField(api, "kernel32", kernel);

        assertEquals(42, api.open("path", 1, 2));
        assertEquals(64L, api.lseek(3, 4L, 5));
        assertEquals(5L, api.read(6, 7L, 8L));
        assertEquals(6L, api.write(9, 10L, 11L));
        assertEquals(12, api.close(13));
        assertEquals(1234, api.getpid());
        assertEquals(9876, api.gettid());
        assertEquals("error", api.strerror(99));

        long ptr = api.malloc(32);
        assertNotEquals(0L, ptr);
        api.free(ptr);

        assertEquals(0, api.madvise(1L, 2L, 3));
        assertEquals(0, api.msync(1L, 2L, 3));
        assertEquals(0, api.fallocate(1, 2, 3L, 4L));
        assertEquals(0, api.ftruncate(1, 2L));
        assertEquals(0L, api.mmap(1L, 2L, 3, 4, 5, 6L));
        assertEquals(0, api.munmap(1L, 2L));
        long timeval = UnsafeMemory.UNSAFE.allocateMemory(16);
        try {
            assertEquals(0, api.gettimeofday(timeval));
        } finally {
            UnsafeMemory.UNSAFE.freeMemory(timeval);
        }
        assertTrue(api.clock_gettime() > 0);
        assertTrue(api.clock_gettime(1) > 0);
        assertEquals(-1, api.lockf(1, 2, 3L));
        assertEquals(-1, api.sched_setaffinity(1, 2, 3L));
        assertEquals(-1, api.sched_getaffinity(1, 2, 3L));
        assertTrue(api.lastError() >= 0);
        assertEquals(Runtime.getRuntime().availableProcessors(), api.get_nprocs_conf());
        assertEquals(api.get_nprocs_conf(), api.get_nprocs());

        assertEquals(Arrays.asList("open", "lseek", "read", "write", "close", "pid", "strerror"), win.calls);
        assertEquals(Arrays.asList("tid"), kernel.calls);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = WinJNRPosixAPI.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static final class RecordingWin implements WinJNRPosixInterface {
        final List<String> calls = new ArrayList<>();

        @Override
        public long malloc(long size) {
            calls.add("malloc");
            return UnsafeMemory.UNSAFE.allocateMemory(size);
        }

        @Override
        public void free(long ptr) {
            calls.add("free");
            UnsafeMemory.UNSAFE.freeMemory(ptr);
        }

        @Override
        public int _close(int fd) {
            calls.add("close");
            return 12;
        }

        @Override
        public int _open(CharSequence path, int flags, int perm) {
            calls.add("open");
            return 42;
        }

        @Override
        public long _lseeki64(int fd, long offset, int origin) {
            calls.add("lseek");
            return 64L;
        }

        @Override
        public long _read(int fd, long dst, long len) {
            calls.add("read");
            return 5L;
        }

        @Override
        public long _write(int fd, long src, long len) {
            calls.add("write");
            return 6L;
        }

        @Override
        public int _getpid() {
            calls.add("pid");
            return 1234;
        }

        @Override
        public String strerror(int errno) {
            calls.add("strerror");
            return "error";
        }
    }

    private static final class RecordingKernel implements Kernel32JNRInterface {
        final List<String> calls = new ArrayList<>();

        @Override
        public int GetCurrentThreadId() {
            calls.add("tid");
            return 9876;
        }

        @Override
        public void GetNativeSystemInfo(long addr) {
            calls.add("sysinfo");
        }
    }
}
