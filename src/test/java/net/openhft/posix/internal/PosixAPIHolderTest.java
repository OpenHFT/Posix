package net.openhft.posix.internal;

import jnr.ffi.LibraryOption;
import jnr.ffi.Platform;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jnr.JNRPosixAPI;
import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.openhft.posix.internal.UnsafeMemory.UNSAFE;
import static org.junit.Assert.*;

/**
 * Tests around {@link PosixAPIHolder} to ensure the documented provider order
 * (POSIX-FN-002) stays enforced and the No-Op fallback is reachable.
 */
public class PosixAPIHolderTest {

    private PosixAPI previous;
    private Platform originalPlatform;

    @Before
    public void setUp() throws Exception {
        previous = PosixAPIHolder.POSIX_API;
        PosixAPIHolder.POSIX_API = null;
        originalPlatform = currentPlatform();
    }

    @After
    public void tearDown() throws Exception {
        PosixAPIHolder.POSIX_API = previous;
        swapPlatform(originalPlatform);
    }

    @Test
    public void loadPosixApiPicksNativeFirst() {
        PosixAPIHolder.loadPosixApi();
        if (Platform.getNativePlatform().isUnix()) {
            assertTrue("Expected JNR provider on Unix", PosixAPIHolder.POSIX_API instanceof JNRPosixAPI);
        } else {
            assertEquals("Expected WinJNR on non-Unix platforms",
                    "net.openhft.posix.internal.jnr.WinJNRPosixAPI",
                    PosixAPIHolder.POSIX_API.getClass().getName());
        }
    }

    @Test
    public void loadPosixApiFallsBackToNoOpWhenNativeFails() throws Exception {
        swapPlatform(new StubPlatform(Platform.OS.WINDOWS, "missing-runtime"));

        PosixAPIHolder.loadPosixApi();

        assertTrue("Expected NoOp fallback but got " + PosixAPIHolder.POSIX_API.getClass().getName(),
                PosixAPIHolder.POSIX_API instanceof NoOpPosixAPI);
    }

    private static Platform currentPlatform() throws Exception {
        Field field = singletonField();
        return (Platform) field.get(null);
    }

    private static void swapPlatform(Platform platform) throws Exception {
        Field field = singletonField();
        Object base = UNSAFE.staticFieldBase(field);
        long offset = UNSAFE.staticFieldOffset(field);
        UNSAFE.putObject(base, offset, platform);
    }

    private static Field singletonField() throws Exception {
        Class<?> holder = Class.forName("jnr.ffi.Platform$SingletonHolder");
        Field field = holder.getDeclaredField("PLATFORM");
        if (!field.canAccess(null)) {
            field.setAccessible(true);
        }
        return field;
    }

    /**
     * Custom Platform so tests can force Windows behaviour irrespective of host OS.
     */
    private static final class StubPlatform extends Platform {
        private final String cLibName;

        StubPlatform(OS os, String cLibName) {
            super(os, CPU.I386, 32, 32, ".*");
            this.cLibName = cLibName;
        }

        @Override
        public String mapLibraryName(String libname) {
            return libname;
        }

        @Override
        public String locateLibrary(String libname, List<String> searchPath) {
            return null;
        }

        @Override
        public String locateLibrary(String libname, List<String> searchPath, Map<LibraryOption, Object> options) {
            return null;
        }

        @Override
        public List<String> libraryLocations(String libname, List<String> searchPath) {
            return Collections.emptyList();
        }

        @Override
        public String getStandardCLibraryName() {
            return cLibName;
        }
    }
}
