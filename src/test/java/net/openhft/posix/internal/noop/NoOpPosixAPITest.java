package net.openhft.posix.internal.noop;

import net.openhft.posix.MAdviseFlag;
import net.openhft.posix.MSyncFlag;
import net.openhft.posix.OpenFlag;
import net.openhft.posix.PosixRuntimeException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Behavioural checks for {@link NoOpPosixAPI}.
 * Verifies which calls return graceful no-ops and which throw so the safety net
 * stays consistent with POSIX-FN-010.
 */
public class NoOpPosixAPITest {

    private final NoOpPosixAPI api = new NoOpPosixAPI("test");

    @Test
    public void noopOperationsReturnSuccessCodes() {
        assertEquals(0, api.fallocate(1, 0, 0, 1));
        assertEquals(0, api.ftruncate(1, 128));
        assertEquals(0, api.madvise(0, 16, MAdviseFlag.MADV_NORMAL.value()));
        assertEquals(0, api.msync(0, 16, MSyncFlag.MS_ASYNC.value()));
        assertEquals(0, api.sched_setaffinity(1234, 8, 0L));
        assertEquals(-1, api.sched_getaffinity(1234, 8, 0L));
        assertEquals(0, api.lastError());
        assertNull(api.strerror(42));
    }

    @Test
    public void exceptionalOperationsThrow() {
        assertThrows(PosixRuntimeException.class, () -> api.open("path", OpenFlag.O_RDONLY.value(), 0644));
        assertThrows(PosixRuntimeException.class, () -> api.read(3, 0L, 16));
        assertThrows(PosixRuntimeException.class, () -> api.write(3, 0L, 16));
        assertThrows(PosixRuntimeException.class, () -> api.gettimeofday(0));
        assertThrows(PosixRuntimeException.class, () -> api.clock_gettime(0));
        assertThrows(PosixRuntimeException.class, () -> api.malloc(4));
        assertThrows(PosixRuntimeException.class, () -> api.getpid());
        assertThrows(PosixRuntimeException.class, () -> api.gettid());
    }
}
