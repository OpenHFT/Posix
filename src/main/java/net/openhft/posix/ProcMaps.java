package net.openhft.posix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

/**
 * Reader for {@code /proc/[pid]/maps}.
 *
 * Utility to parse {@code /proc/[pid]/maps} on Linux.
 * Instantiation fails with an {@link IOException} when run on an operating system
 * that lacks this file.
 * @see <a href="https://man7.org/linux/man-pages/man5/proc.5.html">proc(5)</a>
 */
public final class ProcMaps {
    // A list to hold the memory mappings
    private final List<Mapping> mappingList = new ArrayList<>();

    /**
     * Reads mappings for the given process.
     *
     * @param proc process id or "self"
     * @throws IOException on read failure
     */
    private ProcMaps(Object proc) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/" + proc + "/maps"))) {
            for (String line; (line = br.readLine()) != null; ) {
                mappingList.add(new Mapping(line));
            }
        }
    }

    /**
     * Create a {@link ProcMaps} for the current process.
     *
     * @return mappings for this process
     * @throws IOException on read failure
     */
    public static ProcMaps forSelf() throws IOException {
        return new ProcMaps("self");
    }

    /**
     * Create a {@link ProcMaps} for the given PID.
     *
     * @param pid target process id
     * @return mappings for the process
     * @throws IOException on read failure
     */
    public static ProcMaps forPID(int pid) throws IOException {
        return new ProcMaps(pid);
    }

    /** Immutable list of mappings. */
    public List<Mapping> list() {
        return unmodifiableList(mappingList);
    }

    /**
     * First mapping matching the predicate.
     *
     * @param test filter condition
     * @return the first match
     * @throws java.util.NoSuchElementException if no mapping matches
     */
    public Mapping findFirst(Predicate<? super Mapping> test) {
        return mappingList.stream()
                .filter(test)
                .findFirst()
                .get();
    }

    /**
     * All mappings matching the predicate.
     *
     * @param test filter condition
     * @return list of matches
     */
    public List<Mapping> findAll(Predicate<? super Mapping> test) {
        return mappingList.stream()
                .filter(test)
                .collect(toList());
    }
}
