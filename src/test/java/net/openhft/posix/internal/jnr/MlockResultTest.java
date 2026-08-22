/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix.internal.jnr;

import jnr.constants.platform.Errno;
import jnr.ffi.Platform;
import net.openhft.posix.PosixRuntimeException;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MlockResultTest {

    @Test
    public void expectedResultsAreInterpretedForMlockAndMlock2() {
        assertResult(0, 0, true);
        assertResult(-1, Errno.ENOMEM.intValue(), false);
        assertResult(-1, Errno.EPERM.intValue(), false);
        assertResult(-1, Errno.EAGAIN.intValue(), false);
        assertResult(-1, Errno.ENOSYS.intValue(), false);
        assertFailure(-1, Errno.EINVAL.intValue());
        assertFailure(-1, 9999);
    }

    @Test
    public void syscallNumbersAreSelectedByActualAbi() {
        assertEquals(325, linuxSyscallNumber("x86_64"));
        assertEquals(325, linuxSyscallNumber("amd64"));
        assertEquals(376, linuxSyscallNumber("x86"));
        assertEquals(376, linuxSyscallNumber("i686"));
        assertEquals(390, linuxSyscallNumber("arm"));
        assertEquals(390, linuxSyscallNumber("armv7l"));
        assertEquals(284, linuxSyscallNumber("aarch64"));
        assertEquals(284, linuxSyscallNumber("arm64"));
        assertEquals(284, linuxSyscallNumber("riscv64"));
        assertEquals(-1, linuxSyscallNumber("ppc64le"));
    }

    @Test
    public void linuxSyscallNumbersAreNeverUsedOnAnotherUnixAbi() {
        assertEquals(-1, JNRPosixAPI.mlock2SyscallNumber(Platform.OS.DARWIN, "amd64"));
        assertEquals(-1, JNRPosixAPI.mlock2SyscallNumber(Platform.OS.FREEBSD, "amd64"));
        assertEquals(-1, JNRPosixAPI.mlock2SyscallNumber(Platform.OS.SOLARIS, "amd64"));
    }

    @Test
    public void mlock2PrefersLibcBinding() {
        NativeCalls calls = new NativeCalls();
        calls.mlock2Result = 0;
        JNRPosixAPI api = calls.api(0, 284);

        assertTrue(api.mlock2(1L, 4096L, true));
        assertEquals(1, calls.mlock2Calls.get());
        assertEquals(0, calls.syscallCalls.get());
    }

    @Test
    public void missingBindingUsesAbiSpecificSyscall() {
        NativeCalls calls = new NativeCalls();
        calls.mlock2Unavailable = true;
        calls.syscallResult = 0;
        JNRPosixAPI api = calls.api(0, 284);

        assertTrue(api.mlock2(1L, 4096L, true));
        assertEquals(1, calls.mlock2Calls.get());
        assertEquals(1, calls.syscallCalls.get());
        assertEquals(284, calls.lastSyscallNumber);
    }

    @Test
    public void missingBindingOnUnknownAbiDoesNotInvokeRawSyscall() {
        NativeCalls calls = new NativeCalls();
        calls.mlock2Unavailable = true;
        JNRPosixAPI api = calls.api(0, -1);

        assertFalse(api.mlock2(1L, 4096L, true));
        assertEquals(1, calls.mlock2Calls.get());
        assertEquals(0, calls.syscallCalls.get());
    }

    @Test
    public void lockWithoutOnFaultUsesMlockWrapper() {
        NativeCalls calls = new NativeCalls();
        calls.mlockResult = 0;
        JNRPosixAPI api = calls.api(0, 284);

        assertTrue(api.mlock2(1L, 4096L, false));
        assertEquals(1, calls.mlockCalls.get());
        assertEquals(0, calls.mlock2Calls.get());
    }

    private static void assertResult(int nativeResult, int errno, boolean expected) {
        NativeCalls mlockCalls = new NativeCalls();
        mlockCalls.mlockResult = nativeResult;
        assertEquals(expected, mlockCalls.api(errno, 284).mlock(1L, 4096L));

        NativeCalls mlock2Calls = new NativeCalls();
        mlock2Calls.mlock2Result = nativeResult;
        assertEquals(expected, mlock2Calls.api(errno, 284).mlock2(1L, 4096L, true));
    }

    private static int linuxSyscallNumber(String architecture) {
        return JNRPosixAPI.mlock2SyscallNumber(Platform.OS.LINUX, architecture);
    }

    private static void assertFailure(int nativeResult, int errno) {
        NativeCalls mlockCalls = new NativeCalls();
        mlockCalls.mlockResult = nativeResult;
        assertThrowsErrno(errno, () -> mlockCalls.api(errno, 284).mlock(1L, 4096L));

        NativeCalls mlock2Calls = new NativeCalls();
        mlock2Calls.mlock2Result = nativeResult;
        assertThrowsErrno(errno, () -> mlock2Calls.api(errno, 284).mlock2(1L, 4096L, true));
    }

    private static void assertThrowsErrno(int errno, Runnable operation) {
        try {
            operation.run();
            fail("expected PosixRuntimeException for errno " + errno);
        } catch (PosixRuntimeException expected) {
            assertEquals(errno, expected.errno());
        }
    }

    private static final class NativeCalls implements InvocationHandler {
        final AtomicInteger mlockCalls = new AtomicInteger();
        final AtomicInteger mlock2Calls = new AtomicInteger();
        final AtomicInteger syscallCalls = new AtomicInteger();
        int mlockResult;
        int mlock2Result;
        int syscallResult;
        boolean mlock2Unavailable;
        int lastSyscallNumber = -1;

        JNRPosixAPI api(int errno, int syscallNumber) {
            JNRPosixInterface proxy = (JNRPosixInterface) Proxy.newProxyInstance(
                    JNRPosixInterface.class.getClassLoader(),
                    new Class<?>[]{JNRPosixInterface.class}, this);
            return new JNRPosixAPI(proxy, () -> errno, syscallNumber);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "mlock":
                    mlockCalls.incrementAndGet();
                    return mlockResult;
                case "mlock2":
                    mlock2Calls.incrementAndGet();
                    if (mlock2Unavailable)
                        throw new UnsatisfiedLinkError("mlock2 missing");
                    return mlock2Result;
                case "syscall":
                    syscallCalls.incrementAndGet();
                    lastSyscallNumber = (Integer) args[0];
                    return syscallResult;
                case "gettid":
                    return 1;
                case "toString":
                    return "NativeCalls";
                default:
                    return defaultValue(method.getReturnType());
            }
        }

        private static Object defaultValue(Class<?> type) {
            if (!type.isPrimitive())
                return null;
            if (type == boolean.class)
                return false;
            if (type == byte.class)
                return (byte) 0;
            if (type == short.class)
                return (short) 0;
            if (type == int.class)
                return 0;
            if (type == long.class)
                return 0L;
            if (type == float.class)
                return 0.0f;
            if (type == double.class)
                return 0.0d;
            if (type == char.class)
                return '\0';
            return null;
        }
    }
}
