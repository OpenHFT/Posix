package net.openhft.posix.internal;

import net.openhft.posix.PosixAPI;
import net.openhft.posix.internal.jnr.JNRPosixAPI;
import org.slf4j.LoggerFactory;

public class PosixAPIHolder {
    public static final PosixAPI POSIX_API;

    static {
        PosixAPI posixAPI = null;
        try {
            posixAPI = new JNRPosixAPI();
        } catch (Throwable t) {
            LoggerFactory.getLogger(PosixAPIHolder.class).debug("Unable to load JNRPosixAPI", t);

/*
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
