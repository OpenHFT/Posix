package net.openhft.posix.internal.jnr;

import net.openhft.posix.*;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

import static org.junit.Assert.*;

/*
     380 MB/s -> 31 delays of 100+ us.
     550 MB/s -> 51 delays of 100+ us.
     800 MB/s -> 75 delays of 100+ us.
   1000 MB/s -> 109 delays of 100+ us.
   1450 MB/s -> 331 delays of 100+ us
 */
public class BenchmarkMain {
    private static final Unsafe UNSAFE;

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static void main(String[] args) throws IOException {
        final JNRPosixAPI jnr = new JNRPosixAPI();
        final String filename = "/data/tmp/mmap.file";
        final File file = new File(filename);
        file.delete();
        file.createNewFile();
        final int fd = jnr.open(filename, OpenFlags.O_RDWR.mode(), 0666);
        final long length = 150L << 30;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err);

        assertEquals(0, jnr.du(filename));

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlags.SHARED, fd, 0L);
        assertNotEquals(-1, addr);

        int err4 = jnr.madvise(addr, length, MAdviseFlag.MADV_SEQUENTIAL);
        assertEquals(0, err4);
        int err5 = jnr.madvise(addr, length, MAdviseFlag.MADV_HUGEPAGE);
        assertEquals(0, err5);

        ProcMaps procMaps = ProcMaps.forSelf();
        final List<Mapping> list = procMaps.findAll(m -> m.path().contains(filename));
        assertEquals(1, list.size());
        final Mapping mapping = list.get(0);
        System.out.println(mapping);
        assertEquals(addr, mapping.addr());
        assertEquals(length, mapping.length());
        assertEquals(0L, mapping.offset());


        assertEquals(0, jnr.du(filename));

        AtomicLong upto = new AtomicLong();
        Thread msync = new Thread(() -> {
            try {
                long prev = 0;
                while (true) {
                    long latest = upto.get();
                    long len = latest - prev;
                    if (len > 0) {
                        final int err0 = jnr.msync(addr + prev, len, MSyncFlag.MS_ASYNC);
                        if (err0 == 0) {
                            prev = latest;
                        } else {
                            System.err.println("msync: errno= " + err0);
                            break;
                        }
                    }
                    Thread.sleep(1);
                }
            } catch (InterruptedException ie) {
                System.out.println("msync done.");
            }
        });
        msync.setDaemon(true);
        msync.start();

        int bits = 21;
        for (long i = 0; i < length; i += 1L << bits) {
            int err3 = jnr.fallocate(fd, 0, i, 1L << bits);
            for (int j = 0; j < 1L << bits; j += 4096) {
                UNSAFE.compareAndSwapLong(null, addr + i + j, 0, 0);
            }
            assertEquals(0, err3);
//            assertEquals(length >> 10, jnr.du(filename));
            long start = System.nanoTime();
            for (int j = 0; j < 1L << bits; j += 64) {
                UNSAFE.putLong(addr + i + j, 1);
            }
            long time = System.nanoTime() - start;
            if (time > 100_000)
                System.out.println("i: " + i + ", took " + time / 1000 + " us.");
            upto.set(i + (1L << bits));
            LockSupport.parkNanos(8L << (bits - 2));
        }

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1);
        int err2 = jnr.close(fd);
        assertEquals(0, err2);
        assertTrue(file.exists());
        file.delete();
    }
}
