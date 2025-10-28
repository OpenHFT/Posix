package net.openhft.posix.internal.jnr;

import jnr.constants.platform.Errno;
import jnr.ffi.Pointer;
import net.openhft.posix.MAdviseFlag;
import net.openhft.posix.MSyncFlag;
import net.openhft.posix.MclFlag;
import net.openhft.posix.PosixRuntimeException;
import net.openhft.posix.internal.UnsafeMemory;
import net.openhft.posix.internal.core.Jvm;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import static org.junit.Assert.*;

public class JNRPosixAPINonNativeTest {

    private String originalVmVendor;

    @Before
    public void captureVmVendor() throws Exception {
        originalVmVendor = readStaticString(Jvm.class, "VM_VENDOR");
    }

    @After
    public void restoreVmVendor() throws Exception {
        swapStaticString(Jvm.class, "VM_VENDOR", originalVmVendor);
    }

    @Test
    public void fileLockerAcquiresAndReleases() throws Exception {
        List<Integer> operations = new ArrayList<>();
        JNRPosixInterface stub = new BaseStub() {
            private final Deque<Integer> results = new ArrayDeque<>(Arrays.asList(0, 0));

            @Override
            public int flock(int fd, int operation) {
                operations.add(operation);
                return results.removeFirst();
            }
        };
        JNRPosixAPI api = newApi(stub);

        try (JNRPosixAPI.FileLocker locker = api.new FileLocker(11)) {
            locker.ensureAcquired();
        }

        assertEquals(Arrays.asList(JNRPosixAPI.LOCK_EX, JNRPosixAPI.LOCK_UN), operations);
    }

    @Test
    public void fileLockerFailsToAcquire() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int flock(int fd, int operation) {
                return 1;
            }
        };
        JNRPosixAPI api = newApi(stub);

        try {
            api.new FileLocker(7);
            fail("Expected IOException");
        } catch (IOException expected) {
            assertEquals("Failed to acquire lock", expected.getMessage());
        }
    }

    @Test
    public void fileLockerFailsToRelease() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            private final Deque<Integer> results = new ArrayDeque<>(Arrays.asList(0, 1));

            @Override
            public int flock(int fd, int operation) {
                return results.removeFirst();
            }
        };
        JNRPosixAPI api = newApi(stub);

        JNRPosixAPI.FileLocker locker = api.new FileLocker(3);
        try {
            locker.close();
            fail("Expected IOException");
        } catch (IOException expected) {
            assertEquals("Failed to release lock", expected.getMessage());
        }
    }

    @Test
    public void fallocateFallsBackToPosix() throws Exception {
        AtomicInteger posixCalls = new AtomicInteger();
        List<Integer> flockOps = new ArrayList<>();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int fallocate64(int fd, int mode, long offset, long length) {
                throw new UnsatisfiedLinkError("missing");
            }

            @Override
            public int fallocate(int fd, int mode, long offset, long length) {
                return -1;
            }

            @Override
            public int posix_fallocate(int fd, long offset, long length) {
                posixCalls.incrementAndGet();
                return 0;
            }

            @Override
            public int flock(int fd, int operation) {
                flockOps.add(operation);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        int result = api.fallocate(17, 0, 1L, 2L);

        assertEquals(0, result);
        assertEquals(1, posixCalls.get());
        assertEquals(Arrays.asList(JNRPosixAPI.LOCK_EX, JNRPosixAPI.LOCK_UN), flockOps);
    }

    @Test
    public void fallocateReturnsFailureWhenPosixFallbackFails() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int fallocate64(int fd, int mode, long offset, long length) {
                throw new UnsatisfiedLinkError("missing");
            }

            @Override
            public int fallocate(int fd, int mode, long offset, long length) {
                return -1;
            }

            @Override
            public int posix_fallocate(int fd, long offset, long length) {
                return -1;
            }

            @Override
            public int flock(int fd, int operation) {
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        int result = api.fallocate(21, 0, 4L, 8L);
        assertEquals(-1, result);
    }

    @Test
    public void fallocatePropagatesWhenModeNonZero() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int fallocate(int fd, int mode, long offset, long length) {
                throw new IllegalStateException("boom");
            }
        };
        JNRPosixAPI api = newApi(stub);

        try {
            api.fallocate(30, 1, 0L, 1L);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            assertEquals("boom", expected.getMessage());
        }
    }

    @Test
    public void mlockReturnsTrueOnSuccess() throws Exception {
        AtomicBoolean called = new AtomicBoolean();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int mlock(long addr, long length) {
                called.set(true);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertTrue(api.mlock(10L, 32L));
        assertTrue(called.get());
    }

    @Test
    public void mlockReturnsFalseOnEnomem() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int mlock(long addr, long length) {
                return Errno.ENOMEM.intValue();
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertFalse(api.mlock(5L, 16L));
    }

    @Test
    public void mlockSkipsOnAzul() throws Exception {
        swapStaticString(Jvm.class, "VM_VENDOR", "Azul Systems Prime");
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int mlock(long addr, long length) {
                throw new AssertionError("Should not be called");
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertTrue(api.mlock(7L, 9L));
    }

    @Test
    public void mlock2UsesSyscallWhenRequested() throws Exception {
        AtomicBoolean syscallUsed = new AtomicBoolean();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int mlock(long addr, long length) {
                throw new AssertionError("Should not call mlock");
            }

            @Override
            public int syscall(int number, long arg1, long arg2, int arg3) {
                syscallUsed.set(true);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertTrue(api.mlock2(1L, 2L, true));
        assertTrue(syscallUsed.get());
    }

    @Test
    public void mlock2ReturnsFalseOnEnomem() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int syscall(int number, long arg1, long arg2, int arg3) {
                return Errno.ENOMEM.intValue();
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertFalse(api.mlock2(1L, 2L, true));
    }

    @Test
    public void mlock2FallsBackWhenLockOnFaultFalse() throws Exception {
        AtomicBoolean mlockCalled = new AtomicBoolean();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int mlock(long addr, long length) {
                mlockCalled.set(true);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertTrue(api.mlock2(3L, 4L, false));
        assertTrue(mlockCalled.get());
    }

    @Test
    public void msyncDelegatesToInterface() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int msync(long address, long length, int flags) {
                invoked.set(true);
                assertEquals(10L, address);
                assertEquals(20L, length);
                assertEquals(MSyncFlag.MS_ASYNC.value(), flags);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertEquals(0, api.msync(10L, 20L, MSyncFlag.MS_ASYNC.value()));
        assertTrue(invoked.get());
    }

    @Test
    public void madviseDelegatesToInterface() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int madvise(long addr, long length, int advise) {
                invoked.set(true);
                assertEquals(12L, addr);
                assertEquals(24L, length);
                assertEquals(MAdviseFlag.MADV_SEQUENTIAL.value(), advise);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertEquals(0, api.madvise(12L, 24L, MAdviseFlag.MADV_SEQUENTIAL.value()));
        assertTrue(invoked.get());
    }

    @Test
    public void schedSetAffinityDelegates() throws Exception {
        AtomicBoolean called = new AtomicBoolean();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int sched_setaffinity(int pid, int cpusetsize, Pointer mask) {
                called.set(true);
                assertNotNull(mask);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertEquals(0, api.sched_setaffinity(4, 8, 0xFFL));
        assertTrue(called.get());
    }

    @Test
    public void schedSetAffinityThrowsWhenNativeFails() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int sched_setaffinity(int pid, int cpusetsize, Pointer mask) {
                return -1;
            }

            @Override
            public String strerror(int errno) {
                return "affinity";
            }
        };
        JNRPosixAPI api = newApi(stub);

        try {
            api.sched_setaffinity(5, 8, 0x1L);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("affinity"));
        }
    }

    @Test
    public void schedGetAffinityThrowsWhenNativeFails() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int sched_getaffinity(int pid, int cpusetsize, Pointer mask) {
                return -1;
            }

            @Override
            public String strerror(int errno) {
                return "affinity";
            }
        };
        JNRPosixAPI api = newApi(stub);

        try {
            api.sched_getaffinity(6, 8, 0x2L);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("affinity"));
        }
    }

    @Test
    public void gettidThrowsWhenSupplierNegative() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public String strerror(int errno) {
                return "gettid";
            }
        };
        JNRPosixAPI api = newApi(stub);
        setField(api, "gettid", (IntSupplier) () -> -3);

        try {
            api.gettid();
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("gettid"));
        }
    }

    @Test
    public void getNProcsConfCachesValue() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int get_nprocs_conf() {
                return 8 + calls.getAndIncrement();
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertEquals(8, api.get_nprocs_conf());
        assertEquals(8, api.get_nprocs_conf());
        assertEquals(1, calls.get());
    }

    @Test
    public void clockGettimeThrowsOnNativeFailure() throws Exception {
        List<Long> allocated = new ArrayList<>();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public long malloc(long size) {
                long ptr = UnsafeMemory.UNSAFE.allocateMemory(size);
                allocated.add(ptr);
                return ptr;
            }

            @Override
            public void free(long ptr) {
                UnsafeMemory.UNSAFE.freeMemory(ptr);
                allocated.remove(ptr);
            }

            @Override
            public int clock_gettime(int clockId, long ptr) {
                return -1;
            }

            @Override
            public String strerror(int errno) {
                return "clock";
            }
        };
        JNRPosixAPI api = newApi(stub);

        try {
            api.clock_gettime(1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("clock"));
        }
        assertTrue(allocated.isEmpty());
    }

    @Test
    public void clockGettimeReturnsValue() throws Exception {
        List<Long> allocated = new ArrayList<>();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public long malloc(long size) {
                long ptr = UnsafeMemory.UNSAFE.allocateMemory(size);
                allocated.add(ptr);
                return ptr;
            }

            @Override
            public void free(long ptr) {
                UnsafeMemory.UNSAFE.freeMemory(ptr);
                allocated.remove(ptr);
            }

            @Override
            public int clock_gettime(int clockId, long ptr) {
                UnsafeMemory.UNSAFE.putLong(ptr, 2L);
                UnsafeMemory.UNSAFE.putInt(ptr + 8, 50);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        long nanos = api.clock_gettime(4);
        assertEquals(2_000_000_000L + 50, nanos);
        assertTrue(allocated.isEmpty());
    }

    @Test
    public void gettimeofdayReturnsMicros() throws Exception {
        List<Long> allocated = new ArrayList<>();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public long malloc(long size) {
                long ptr = UnsafeMemory.UNSAFE.allocateMemory(size);
                allocated.add(ptr);
                return ptr;
            }

            @Override
            public void free(long ptr) {
                UnsafeMemory.UNSAFE.freeMemory(ptr);
                allocated.remove(ptr);
            }

            @Override
            public int gettimeofday(long timeval, long alwaysNull) {
                UnsafeMemory.UNSAFE.putLong(timeval, 7L);
                UnsafeMemory.UNSAFE.putLong(timeval + 8, 250_000L);
                return 0;
            }
        };
        JNRPosixAPI api = newApi(stub);

        long micros = api.gettimeofday();
        assertEquals(7_000_000L + 250_000L, micros);
        assertTrue(allocated.isEmpty());
    }

    @Test
    public void mlockallThrowsOnFailure() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int mlockall(int flags) {
                return -1;
            }

            @Override
            public String strerror(int errno) {
                return "mlockall";
            }
        };
        JNRPosixAPI api = newApi(stub);

        try {
            api.mlockall(0x123);
            fail("Expected PosixRuntimeException");
        } catch (IllegalArgumentException | PosixRuntimeException expected) {
            assertTrue(expected.getMessage().contains("mlockall"));
        }
    }

    @Test
    public void mmapReturnsPointer() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset) {
                return 123L;
            }
        };
        JNRPosixAPI api = newApi(stub);

        assertEquals(123L, api.mmap(0L, 64L, 1, 2, 3, 4L));
    }

    @Test
    public void mmapThrowsOnFailure() throws Exception {
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset) {
                return 0L;
            }
        };
        JNRPosixAPI api = newApi(stub);

        try {
            api.mmap(1L, 64L, 1, 2, 3, 4L);
            fail("Expected PosixRuntimeException");
        } catch (PosixRuntimeException expected) {
            assertFalse(expected.getMessage().isEmpty());
        }
    }

    @Test
    public void getGettidFallsBackToSyscallOn32Bit() throws Exception {
        boolean original32 = readStaticBoolean(UnsafeMemory.class, "IS32BIT");
        boolean original64 = readStaticBoolean(UnsafeMemory.class, "IS64BIT");
        swapStaticBoolean(UnsafeMemory.class, "IS32BIT", true);
        swapStaticBoolean(UnsafeMemory.class, "IS64BIT", false);
        try {
            AtomicInteger calls = new AtomicInteger();
            JNRPosixInterface stub = new BaseStub() {
                @Override
                public int gettid() {
                    throw new UnsatisfiedLinkError("no symbol");
                }

                @Override
                public int syscall(int number) {
                    assertEquals(224, number);
                    calls.incrementAndGet();
                    return 99;
                }
            };
            JNRPosixAPI api = newApi(stub);
            Method method = JNRPosixAPI.class.getDeclaredMethod("getGettid");
            method.setAccessible(true);
            IntSupplier supplier = (IntSupplier) method.invoke(api);
            assertEquals(99, supplier.getAsInt());
            assertEquals(1, calls.get());
        } finally {
            swapStaticBoolean(UnsafeMemory.class, "IS32BIT", original32);
            swapStaticBoolean(UnsafeMemory.class, "IS64BIT", original64);
        }
    }

    @Test
    public void mlockallLogsWhenDumpEnabled() throws Exception {
        boolean originalDump = swapStaticBoolean(JNRPosixAPI.class, "MOCKALL_DUMP", true);
        String previousProperty = System.getProperty("mlockall.dump");
        System.setProperty("mlockall.dump", "true");
        AtomicBoolean firstCall = new AtomicBoolean(true);
        AtomicInteger calls = new AtomicInteger();
        JNRPosixInterface stub = new BaseStub() {
            @Override
            public int mlock(long addr, long length) {
                calls.incrementAndGet();
                if (firstCall.getAndSet(false))
                    return -1;
                return 0;
            }

            @Override
            public int mlockall(int flags) {
                throw new AssertionError("mlockall should not be called in fallback");
            }
        };
        JNRPosixAPI api = newApi(stub);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(baos));
        try {
            api.mlockall(MclFlag.MclCurrent.code());
        } finally {
            System.setOut(originalOut);
            if (previousProperty == null) {
                System.clearProperty("mlockall.dump");
            } else {
                System.setProperty("mlockall.dump", previousProperty);
            }
            swapStaticBoolean(JNRPosixAPI.class, "MOCKALL_DUMP", originalDump);
        }
        assertTrue(calls.get() > 0);
        assertTrue(baos.toString().length() > 0);
    }

    private static JNRPosixAPI newApi(JNRPosixInterface stub) throws Exception {
        JNRPosixAPI api = (JNRPosixAPI) UnsafeMemory.UNSAFE.allocateInstance(JNRPosixAPI.class);
        setField(api, "jnr", stub);
        setField(api, "gettid", (IntSupplier) () -> 1);
        return api;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JNRPosixAPI.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static boolean swapStaticBoolean(Class<?> type, String fieldName, boolean newValue) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object base = UnsafeMemory.UNSAFE.staticFieldBase(field);
        long offset = UnsafeMemory.UNSAFE.staticFieldOffset(field);
        boolean old = UnsafeMemory.UNSAFE.getBoolean(base, offset);
        UnsafeMemory.UNSAFE.putBoolean(base, offset, newValue);
        return old;
    }

    private static boolean readStaticBoolean(Class<?> type, String fieldName) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object base = UnsafeMemory.UNSAFE.staticFieldBase(field);
        long offset = UnsafeMemory.UNSAFE.staticFieldOffset(field);
        return UnsafeMemory.UNSAFE.getBoolean(base, offset);
    }

    private static String swapStaticString(Class<?> type, String fieldName, Object newValue) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object base = UnsafeMemory.UNSAFE.staticFieldBase(field);
        long offset = UnsafeMemory.UNSAFE.staticFieldOffset(field);
        Object old = UnsafeMemory.UNSAFE.getObject(base, offset);
        UnsafeMemory.UNSAFE.putObject(base, offset, newValue);
        return old == null ? null : old.toString();
    }

    private static String readStaticString(Class<?> type, String fieldName) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        field.setAccessible(true);
        Object base = UnsafeMemory.UNSAFE.staticFieldBase(field);
        long offset = UnsafeMemory.UNSAFE.staticFieldOffset(field);
        Object value = UnsafeMemory.UNSAFE.getObject(base, offset);
        return value == null ? null : value.toString();
    }

    private static class BaseStub implements JNRPosixInterface {
        @Override
        public int open(CharSequence path, int flags, int perm) {
            throw unsupported();
        }

        @Override
        public long read(int fd, long dst, long len) {
            throw unsupported();
        }

        @Override
        public long write(int fd, long src, long len) {
            throw unsupported();
        }

        @Override
        public long lseek(int fd, long offset, int whence) {
            throw unsupported();
        }

        @Override
        public int lockf(int fd, int cmd, long len) {
            throw unsupported();
        }

        @Override
        public int flock(int fd, int operation) {
            throw unsupported();
        }

        @Override
        public int ftruncate(int fd, long offset) {
            throw unsupported();
        }

        @Override
        public int fallocate(int fd, int mode, long offset, long length) {
            throw unsupported();
        }

        @Override
        public int fallocate64(int fd, int mode, long offset, long length) {
            throw unsupported();
        }

        @Override
        public int posix_fallocate(int fd, long offset, long length) {
            throw unsupported();
        }

        @Override
        public int close(int fd) {
            throw unsupported();
        }

        @Override
        public int madvise(long addr, long length, int advise) {
            throw unsupported();
        }

        @Override
        public long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset) {
            throw unsupported();
        }

        @Override
        public int munmap(long addr, long length) {
            throw unsupported();
        }

        @Override
        public int msync(long address, long length, int flags) {
            throw unsupported();
        }

        @Override
        public int gettimeofday(long timeval, long alwaysNull) {
            throw unsupported();
        }

        @Override
        public long malloc(long size) {
            throw unsupported();
        }

        @Override
        public void free(long ptr) {
            throw unsupported();
        }

        @Override
        public int get_nprocs() {
            throw unsupported();
        }

        @Override
        public int get_nprocs_conf() {
            throw unsupported();
        }

        @Override
        public int sched_setaffinity(int pid, int cpusetsize, Pointer mask) {
            throw unsupported();
        }

        @Override
        public int sched_getaffinity(int pid, int cpusetsize, Pointer mask) {
            throw unsupported();
        }

        @Override
        public int getpid() {
            throw unsupported();
        }

        @Override
        public int gettid() {
            throw unsupported();
        }

        @Override
        public String strerror(int errno) {
            throw unsupported();
        }

        @Override
        public int clock_gettime(int clockId, long ptr) {
            throw unsupported();
        }

        @Override
        public int mlock(long addr, long length) {
            throw unsupported();
        }

        @Override
        public int mlock2(long addr, long length, int flags) {
            throw unsupported();
        }

        @Override
        public int mlockall(int flags) {
            throw unsupported();
        }

        @Override
        public int syscall(int number) {
            throw unsupported();
        }

        @Override
        public int syscall(int number, long arg1, long arg2, int arg3) {
            throw unsupported();
        }

        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("not expected");
        }
    }
}
