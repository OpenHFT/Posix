/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
package net.openhft.posix;

import net.openhft.posix.internal.jna.WinJNAPosixAPI;
import net.openhft.posix.internal.jnr.WinJNRPosixAPI;

/** Child process for native lock contention; never blocks waiting for a lock. */
public class WindowsLockChild {
    public static void main(String[] args) throws Exception {
        PosixAPI api =
                "jnr".equals(args[0])
                        ? new WinJNRPosixAPI()
                        : "jna".equals(args[0])
                                ? new WinJNAPosixAPI()
                                : "jna-direct".equals(args[0])
                                        ? new net.openhft.posix.internal.jna.WinJNADirectPosixAPI()
                                        : (PosixAPI)
                                                Class.forName(
                                                                "net.openhft.posix.ffm.WinFFMPosixAPI")
                                                        .getDeclaredConstructor()
                                                        .newInstance();
        int fd = api.open(args[1], OpenFlag.O_RDWR, 0600);
        if (fd < 0) throw new AssertionError(api.lastErrorStr());
        try {
            int result = api.lockf(fd, 2, 32);
            if ("blocked".equals(args[2])) {
                if (result != -1 || api.lastError() == 0)
                    throw new AssertionError("Lock contention not reported");
            } else {
                if (result != 0) throw new AssertionError(api.lastErrorStr());
                if (api.lockf(fd, 0, 32) != 0) throw new AssertionError("Unlock failed");
            }
        } finally {
            api.close(fd);
        }
    }
}
