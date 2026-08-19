# PR: Posix#15 (CLOSE_FIXED)

**Track D — Successor/regression PR behind a blocked close**  ·  priority tier 3  ·  base `ea`  ·  branch `test/Posix-15-close-as-completed`
Issue: https://github.com/OpenHFT/Posix/issues/15

## Planned change
Close as completed

## Quality bar (must clear before this PR merges)
- One issue, one PR; link with `Fixes #15`; scope limited to this issue.
- Regression test that fails before / passes after (re-enable the ignored test where one exists, else add one).
- Cross-repo discipline: Chronicle-Queue exposes integration points only; retention/roll-maintenance policy lives in CQE.
- Do not fork the in-flight QUEUE-143/144 PRs; branch off current `ea` and rebase.
- Docs + changelog updated; author credit retained on any rebase; CI proven green.

## Status
BLOCKED: precondition must clear first (see plan) — this branch scaffolds the successor/test.
_This PR-NOTES commit is the local addressment scaffold; the code change lands on top._
