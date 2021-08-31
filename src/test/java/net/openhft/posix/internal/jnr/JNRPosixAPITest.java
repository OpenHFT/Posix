package net.openhft.posix.internal.jnr;

import net.openhft.posix.*;
import net.openhft.posix.internal.jna.JNAPosixAPI;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class JNRPosixAPITest {

    JNRPosixAPI jnr = new JNRPosixAPI();
    JNAPosixAPI jna = new JNAPosixAPI();

    public static long diskUsage(String filename) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("du", filename);
        pb.redirectErrorStream(true);
        final Process process = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = br.readLine();
            return Long.parseUnsignedLong(line.split("\\s+")[0]);
        }
    }

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

        assertEquals(0, diskUsage(filename));

        long addr = jnr.mmap(0, length, MMapProt.PROT_READ_WRITE, MMapFlags.SHARED, fd, 0L);
        assertNotEquals(-1, addr);

        ProcMaps procMaps = ProcMaps.forSelf();
        final List<Mapping> list = procMaps.findAll(m -> filename.equals(m.path()));
        assertEquals(1, list.size());
        final Mapping mapping = list.get(0);
        assertEquals(addr, mapping.addr());
        assertEquals(length, mapping.length());
        assertEquals(0L, mapping.offset());

        assertEquals(0, diskUsage(filename));
        int err3 = jnr.fallocate(fd, 0, 0, length);
        assertEquals(0, err3);
        assertEquals(length >> 10, diskUsage(filename));

        final int err0 = jnr.msync(addr, length, MSyncFlag.MS_ASYNC);
        assertEquals(0, err0);

        int err1 = jnr.munmap(addr, length);
        assertEquals(0, err1);
        int err2 = jnr.close(fd);
        assertEquals(0, err2);
        assertTrue(file.toFile().exists());
        file.toFile().delete();
    }
}