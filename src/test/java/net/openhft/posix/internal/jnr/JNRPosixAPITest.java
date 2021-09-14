package net.openhft.posix.internal.jnr;

import net.openhft.posix.*;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class JNRPosixAPITest {

    static final JNRPosixAPI jnr = new JNRPosixAPI();

    @Test
    public void open() throws IOException {
        final Path file = Files.createTempFile("open", ".test");
        final int fd = jnr.open(file.toString(), OpenFlags.O_RDWR, 0666);
        int err = jnr.close(fd);
        assertEquals(0, err);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }

    @Test
    public void close() {
        int err = jnr.close(-1);
        assertEquals(-1, err);
    }

    @Test
    public void mmap_sync() throws IOException {
        final Path file = Files.createTempFile("mmap", ".test");
        final String filename = file.toAbsolutePath().toString();
        final int fd = jnr.open(filename, OpenFlags.O_RDWR, 0666);
        final long length = 1L << 16;
        int err = jnr.ftruncate(fd, length);
        assertEquals(0, err);

        assertEquals(0, jnr.du(filename));

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlags.SHARED, fd, 0L);
        assertNotEquals(-1, addr);

        int err4 = jnr.madvise(addr, length, MAdviseFlag.MADV_SEQUENTIAL);
        assertEquals(0, err4);

        ProcMaps procMaps = ProcMaps.forSelf();
        final List<Mapping> list = procMaps.findAll(m -> filename.equals(m.path()));
        assertEquals(1, list.size());
        final Mapping mapping = list.get(0);
        assertEquals(addr, mapping.addr());
        assertEquals(length, mapping.length());
        assertEquals(0L, mapping.offset());

        assertEquals(0, jnr.du(filename));
        int err3 = jnr.fallocate(fd, 0, 0, length);
        assertEquals(0, err3);
        assertEquals(length >> 10, jnr.du(filename));

        final int err0 = jnr.msync(addr, length, MSyncFlag.MS_ASYNC);
        assertEquals(0, err0);

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1);
        int err2 = jnr.close(fd);
        assertEquals(0, err2);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }

    @Test
    public void gettimeofday() {
        long time = jnr.gettimeofday();
        assertNotEquals(0, time);
        assertEquals(System.currentTimeMillis() * 1_000, time, 1000);
        System.out.println(time);
    }

    @Test
    public void get_nprod() {
        final int nprocs = jnr.get_nprocs();
        assertTrue(nprocs > 0);
        final int nprocs_conf = jnr.get_nprocs_conf();
        assertTrue(nprocs <= nprocs_conf);
    }

    @Test
    public void getpid() {
        final int nprocs = jnr.get_nprocs();
        final int[] ints = IntStream.range(0, nprocs * 101)
                .parallel()
                .map(i -> jnr.gettid())
                .sorted()
                .distinct()
                .toArray();
        System.out.println(Arrays.toString(ints));
    }

    @Test
    public void setaffinity() {
        try {
            assertEquals(0, jnr.sched_setaffinity_as(jnr.gettid(), 1));
            assertEquals("1-1", jnr.sched_getaffinity_summary(jnr.gettid()));
            assertEquals(0, jnr.sched_setaffinity_range(jnr.gettid(), 2, 4));
            assertEquals("2-3", jnr.sched_getaffinity_summary(jnr.gettid()));
        } finally {
            assertEquals(0, jnr.sched_setaffinity_range(jnr.gettid(), 0, jnr.get_nprocs_conf()));
        }
    }
}