/**
 * Provides a portable subset of POSIX operations.
 * The provider is chosen at initialisation time via
 * {@link net.openhft.posix.internal.PosixAPIHolder} so the same code works
 * across Linux, macOS and Windows.
 *
 * Usage example:
 * <pre>
 *     PosixAPI posix = PosixAPI.posix();
 *     long ptr = posix.malloc(128);
 *     posix.free(ptr);
 * </pre>
 *
 * All helpers strive for zero heap allocation.
 */
package net.openhft.posix;
