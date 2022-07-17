package net.openhft.posix.internal.jnr;

import net.openhft.posix.MMapFlag;
import net.openhft.posix.MMapProt;
import net.openhft.posix.MSyncFlag;
import net.openhft.posix.OpenFlag;
import net.openhft.posix.util.Histogram;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;

/*
 on a Ryzen 5950X, Ubuntu 21.10

 Corsair MP600 PRO XT M.2
 path: /data/tmp, sync: 50/90 97/99 99.7/99.9 99.97/99.99 - worst was 247.0 / 301  385 / 476  775 / 783  1411 / 7,970 - 7,970

 Samsung SSD 870
 path: /ssd/tmp, sync: 50/90 97/99 99.7/99.9 99.97/99.99 - worst was 894 / 912  923 / 945  1149 / 4,400  4,610 / 4,630 - 4,630

 Seagate ST1000DM010-2EP1 HDD
 path: /hdd/tmp, sync: 50/90 97/99 99.7/99.9 99.97/99.99 - worst was 7,270 / 7,660  48,960 / 65,990  98,170 / 107,350  107,610 / 107,610 - 107,610

 Corsair Force MP600
 path: /var/tmp, sync: 50/90 97/99 99.7/99.9 99.97/99.99 - worst was 312 / 328  354 / 385  572 / 4,600  4,610 / 4,730 - 4,730

 tmpfs
 path: /tmp, sync: 50/90 97/99 99.7/99.9 99.97/99.99 - worst was 0.190 / 0.210  0.300 / 0.420  1.130 / 1.150  1.282 / 25.31 - 25.31
 */

public class MSyncFileBenchmarkMain {
    static final String PATH = System.getProperty("path", "/tmp");
    static final int LENGTH = Integer.getInteger("length", 64 << 10);
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

    public static void main(String[] args) throws IOException, InterruptedException {
        for (String path : PATH.split(";")) {
            final String filename = path + "/mmap.file";
            final File file = new File(filename);
            file.delete();
            file.createNewFile();

            final JNRPosixAPI jnr = new JNRPosixAPI();
            final int fd = jnr.open(filename, OpenFlag.O_RDWR, 0666);
            int err = jnr.ftruncate(fd, LENGTH);
            assertEquals(0, err);
            long addr = jnr.mmap(0, LENGTH, MMapProt.PROT_READ_WRITE, MMapFlag.SHARED, fd, 0L);
            Histogram syncTime = new Histogram();
            int warmup = 1;
            long start0 = System.currentTimeMillis();
            for (int runs = -warmup; runs < 10_000; runs++) {
                long start = System.nanoTime();
                // touch all the pages
                for (int offset = 0; offset < LENGTH; offset += 4096) {
                    UNSAFE.putLong(addr + offset, runs);
                }
                jnr.msync(addr, LENGTH, MSyncFlag.MS_SYNC);
                long time = System.nanoTime() - start;
                syncTime.sample(time);
                if (runs == -1)
                    syncTime.reset();
                Thread.sleep(1);
                if (start0 + 30_000 < System.currentTimeMillis())
                    break;
            }
            System.out.println("path: " + path + ", sync: " + syncTime.toLongMicrosFormat());
            int err2 = jnr.close(fd);
            assertEquals(0, err2);
        }
    }
}
