# Upstream synchronization

Before code changes, follow the mandatory sync procedure in [AGENTS.md](../AGENTS.md). Fetch upstream, compare ancestry and merge missing default-branch commits without rewriting fork history. Review conflicts and rerun affected tests. Report blocked/offline checks explicitly.

## Last verified: 2026-10-17

- Upstream: `https://github.com/recloudstream/cloudstream.git`
- Default branch checked: `master`
- Commit: `d9c55e2e5b020dde8e28c12a3e7802df962265ac`
- Result: already an ancestor of the fork; no missing default-branch commits and no merge required before branding/main-branch work.

Do not treat this record as permission to skip fetching on the next task. Check the upstream default branch again if it is renamed.
