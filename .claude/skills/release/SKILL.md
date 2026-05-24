---
name: release
description: Cut a new GitHub release for photographic-indexer that triggers the Docker image build/publish workflow (.github/workflows/release.yml). Use when the user asks to publish a release, ship a version, or tag a release.
---

# リリース手順

このスキルは `master` から新しい GitHub リリースを作成し、`release.yml` ワークフローによる Docker イメージ (`photographic-indexer`, `photographic-indexer-view`, `clip-server`) の Docker Hub / GHCR への publish をトリガーする。

## 1. バージョン番号を確認

**必ずユーザーに次のバージョン番号を尋ねる。** 推測しない。

参考として現在の最新タグを示す：

```shell
git tag --sort=-version:refname | head -5
```

## 2. 差分とリリースノートのスタイル確認

```shell
git log --oneline <prev-tag>..HEAD
gh release view <prev-tag>
```

過去のリリースノートは `## Changes` の下に `- **タイトル** - 一行説明` の bullet list 形式。タイトルはコミットメッセージ調、説明は何が変わったかを補足する。雑多な lint / 整形コミットは1行にまとめてよい。

## 3. リリース作成

```shell
gh release create <new-tag> \
  --target master \
  --title "<new-tag>" \
  --latest \
  --notes "$(cat <<'EOF'
## Changes

- **<title>** - <description>
- ...
EOF
)"
```

注意点：

- `gh release create` には `--notify` フラグは存在しない（`--latest` でよい）
- タグは `v` prefix を付けない（過去の慣習に揃える: `1.4.1`, `1.5.0`）
- `--target master` を必ず指定（HEAD が他ブランチでも `master` の tip からタグを切る）

## 4. ワークフロー起動を確認

```shell
gh run list --workflow=release.yml --limit 3
```

最新行が `in_progress` で `1.5.0` のように対象タグを示していれば成功。失敗時は `gh run view <run-id> --log-failed` で確認。

## 出力

最後にユーザーへ以下を提示する：

- Release URL: `https://github.com/ponkotuy/photographic-indexer/releases/tag/<new-tag>`
- Workflow run の状態（`in_progress` / `completed` / 失敗時はエラー要約）
