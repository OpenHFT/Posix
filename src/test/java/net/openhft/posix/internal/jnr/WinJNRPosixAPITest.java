package net.openhft.posix.internal.jnr;

import org.junit.Test;

public class WinJNRPosixAPITest {
    @Test
    public void gettid() {
        WinJNRPosixAPI api = new WinJNRPosixAPI();
        System.out.println(api.get_nprocs_conf());
    }

}