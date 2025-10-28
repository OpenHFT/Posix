package net.openhft.posix.internal.jna;

import com.sun.jna.Pointer;
import net.openhft.posix.internal.ReflectionAccess;
import org.junit.Assume;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;
import static org.junit.Assert.assertEquals;

/**
 * Smoke tests for the JNA-backed provider to ensure constructor wiring and the
 * {@link JNAPosixAPI#mmap(long, long, int, int, int, long)} wrapper behave.
 */
public class JNAPosixAPITest {

    @Test
    public void mmapUsesNullPointerForZeroAddress() throws Exception {
        DummyJNAPosixAPI api;
        try {
            api = new DummyJNAPosixAPI();
        } catch (UnsatisfiedLinkError | RuntimeException e) {
            Assume.assumeNoException("Skip when libc cannot be loaded in this environment", e);
            return; // kept for static analysis
        }

        RecordingJna stub = new RecordingJna();
        injectStub(api, stub);

        long zeroResult = api.mmap(0L, 64, 1, 2, 3, 4);
        assertEquals(RecordingJna.ZERO_RESULT, zeroResult);
        assertEquals(Pointer.NULL, stub.pointers.get(0));

        long nonZeroResult = api.mmap(64L, 32, 5, 6, 7, 8);
        assertEquals(RecordingJna.NON_ZERO_RESULT, nonZeroResult);
        assertEquals(64L, Pointer.nativeValue(stub.pointers.get(1)));
    }

    private static void injectStub(JNAPosixAPI api, JNAPosixInterface stub) throws Exception {
        Field field = JNAPosixAPI.class.getDeclaredField("jna");
        ReflectionAccess.ensureAccessible(field, api);
        long offset = UNSAFE.objectFieldOffset(field);
        UNSAFE.putObject(api, offset, stub);
    }

    private static final class RecordingJna extends JNAPosixInterface {
        static final long ZERO_RESULT = 111;
        static final long NON_ZERO_RESULT = 222;

        final ArrayList<Pointer> pointers = new ArrayList<>();

        @Override
        public long mmap(Pointer addr, long length, int prot, int flags, int fd, long offset) {
            pointers.add(addr);
            return Pointer.nativeValue(addr) == 0 ? ZERO_RESULT : NON_ZERO_RESULT;
        }
    }

    /**
     * Thin stub so we do not have to exercise the full native surface while still
     * initialising {@link JNAPosixAPI}.
     */
    private static final class DummyJNAPosixAPI extends JNAPosixAPI {
        private UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Only mmap is exercised in this test");
        }

        @Override
        public int open(CharSequence path, int flags, int perm) {
            throw unsupported();
        }

        @Override
        public long lseek(int fd, long offset, int whence) {
            throw unsupported();
        }

        @Override
        public int ftruncate(int fd, long offset) {
            throw unsupported();
        }

        @Override
        public int lockf(int fd, int cmd, long len) {
            throw unsupported();
        }

        @Override
        public int close(int fd) {
            throw unsupported();
        }

        @Override
        public int fallocate(int fd, int mode, long offset, long length) {
            throw unsupported();
        }

        @Override
        public int madvise(long addr, long length, int advice) {
            throw unsupported();
        }

        @Override
        public int msync(long address, long length, int mode) {
            throw unsupported();
        }

        @Override
        public int munmap(long addr, long length) {
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
        public int gettimeofday(long timeval) {
            throw unsupported();
        }

        @Override
        public int sched_setaffinity(int pid, int cpusetsize, long mask) {
            throw unsupported();
        }

        @Override
        public int sched_getaffinity(int pid, int cpusetsize, long mask) {
            throw unsupported();
        }

        @Override
        public int lastError() {
            throw unsupported();
        }

        @Override
        public long clock_gettime(int clockId) throws IllegalArgumentException {
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
    }
}
