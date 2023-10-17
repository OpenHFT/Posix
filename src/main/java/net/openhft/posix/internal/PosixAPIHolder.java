package net.openhft.posix.internal;

import jnr.ffi.Platform;
import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jnr.JNRPosixAPI;
import net.openhft.posix.internal.jnr.WinJNRPosixAPI;
import net.openhft.posix.internal.noop.NoOpPosixAPI;

public class PosixAPIHolder {
    public static PosixAPI POSIX_API;

    public static void loadPosixApi() {
        if (POSIX_API != null)
            return;
        PosixAPI posixAPI;
        try {
            posixAPI = Platform.getNativePlatform().isUnix()
                    ? new JNRPosixAPI()
                    : new WinJNRPosixAPI();
        } catch (Throwable t) {
            posixAPI = new NoOpPosixAPI(t.toString());
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

    public static void useNoOpPosixApi() {
        POSIX_API = new NoOpPosixAPI("Explicitly disabled");
    }
}
