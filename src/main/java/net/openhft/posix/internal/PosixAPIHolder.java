package net.openhft.posix.internal;

import jnr.ffi.Platform;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jnr.JNRPosixAPI;
import net.openhft.posix.internal.jnr.WinJNRPosixAPI;
import net.openhft.posix.internal.noop.NoOpPosixAPI;
import org.slf4j.LoggerFactory;

public class PosixAPIHolder {
    public static final PosixAPI POSIX_API;

    static {
        PosixAPI posixAPI = null;
        try {
            posixAPI = Platform.getNativePlatform().isUnix()
                    ? new JNRPosixAPI()
                    : new WinJNRPosixAPI();
        } catch (Throwable t) {
            LoggerFactory.getLogger(PosixAPIHolder.class).warn("Unable to load JNRPosixAPI", t);
            posixAPI = new NoOpPosixAPI();
/*
            this is commented out it has not been tested yet
            try {
                posixAPI = new JNAPosixAPI();
            } catch (Throwable t2) {
                LoggerFactory.getLogger(PosixAPIHolder.class).debug("Unable to load JNAPosixAPI", t2);
                posixAPI = new RawPosixAPI();
            }
*/
        }
        POSIX_API = posixAPI;
    }
}
