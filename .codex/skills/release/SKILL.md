---
name: release
description: Create GitHub releases for this project or similar GitHub repositories. Use when the user asks to release the current master/main branch, create a version tag, publish a GitHub Release, write release notes, check release workflow status, or follow the repository's release process.
---

# Release

## Workflow

1. Confirm the target branch, version, and tag style.
   - Prefer the current branch only when the user explicitly asks for it. For this repository, releases are cut from `master`.
   - Inspect existing tags with `git tag --sort=-version:refname` and `git ls-remote --tags origin`.
   - Match the existing tag format. This repository uses plain versions such as `1.5.5`, not `v1.5.5`.

2. Verify local and remote state.
   - Run `git status --short`; do not release from a dirty worktree unless the user explicitly approves.
   - Run `git rev-parse HEAD` and `git ls-remote origin refs/heads/<branch>`; confirm the requested release target matches the remote branch tip.
   - If local is behind or differs from remote, stop and ask the user how to proceed.

3. Review changes since the previous release.
   - Identify the previous tag from existing releases/tags.
   - Use `git log --oneline <previous-tag>..HEAD`.
   - Inspect meaningful commits with `git show --stat` or `git show --name-only` as needed.
   - Prefer release notes based on actual changes over generated notes alone.

4. Write curated release notes.
   - Do not rely only on `--generate-notes` when the user expects readable Changes.
   - Match the established format used by older releases such as `1.5.3`:

```markdown
## Changes

- **Short change title** - Concrete user-facing or operational impact
- **Another change title** - Concrete impact
```

   - Keep bullets concise, accurate, and grounded in commits.
   - Include only notable changes; avoid noisy internal churn unless it affects users or operations.

5. Create the GitHub Release.
   - Verify the tag/release does not already exist with `gh release view <version>`.
   - Use `gh release create <version> --target <branch> --title <version> --notes-file <file>` when writing notes from a temp file.
   - If a release already exists, ask before editing, deleting, or recreating it.

6. Confirm creation and report deployment status policy.
   - Verify the release with `gh release view <version> --json tagName,name,url,targetCommitish`.
   - Verify the tag points at the intended commit with `git ls-remote --tags origin refs/tags/<version>`.
   - If the repository triggers deployment or publish workflows, check that they started with `gh run list`.
   - Do not wait for deploy/publish completion by default. Tell the user the workflow has started and ask whether they want you to wait or inspect progress.

## Safety

- Release commands affect shared GitHub state. Use exact version and branch names from the user or repository history.
- Do not invent release notes from memory. Read commits and existing release style first.
- Do not create a different tag prefix than existing releases without asking.
- Do not wait on long-running deployment workflows unless the user asks you to monitor them.
