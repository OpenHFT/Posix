# AGENTS.md

## Scope
- OpenHFT Posix is a low-latency, zero-GC Java facade over a portable POSIX subset.
- Providers include JNR, JNA, raw reflection, and NoOp fallback for CI sandboxes.

## Build and test
- Preferred full check:
  - `mkdir -p logs`
  - `mvn verify -l logs/mvn-verify.log`
- Test example:
  - `mvn -Dtest=ClassName test -l logs/mvn-test.log`
- Review logs:
  - `rg -n '^\[(WARNING|ERROR)\]|SLF4J\(W\)|\bWARNING:|\bwarning:' logs/mvn-verify.log`
- Do not commit logs/.

## Constraints
- Java baseline: 8 (avoid newer language features).
- Source files must stay ISO-8859-1 (code points 0-255). Prefer ASCII; avoid smart quotes and non-breaking spaces.
- Preserve public APIs unless explicitly requested.
- Treat warnings as defects; keep logs clean.
- Avoid extra allocations or synchronisation on hot paths.
- If you skip a test suite, say so and why.

## Docs and review checklist
- Keep AsciiDoc, tests, and code in sync; update `src/main/adoc` when behaviour changes.
- Javadoc must add behavioural contracts, edge cases, thread safety, units, or performance notes.
- For large mechanical changes, declare the transformation rule and keep it consistent.

## References
- Environment variables:
  - `POSIX_TEST_ALLOW_NATIVE=false` to force `NoOpPosixAPI` in CI.
  - `POSIX_SYSLOG_LEVEL` to adjust provider logging noise.
- `src/main/adoc/decision-log.adoc` and `src/main/adoc/project-requirements.adoc`.
- `OpenHFT/docs/Company-Wide-Tagging.adoc` for tagging and AsciiDoc conventions.
