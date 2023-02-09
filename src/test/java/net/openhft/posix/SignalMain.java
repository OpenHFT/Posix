package net.openhft.posix;

import net.openhft.posix.internal.PosixAPIHolder;

import java.util.concurrent.locks.LockSupport;

public class SignalMain {

    public static void main(String[] args) {

        // 10) SIGUSR1

        final PosixAPI posix = PosixAPIHolder.POSIX_API;
        PosixAPI.SignalFunction func = signal -> System.out.println("received signal=" + signal);
        int signal = Integer.parseInt(args[0]);
        System.out.println("registering signal " + signal);
        posix.signal(signal, func);
        LockSupport.park();
    }
}
