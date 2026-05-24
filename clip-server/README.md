# clip-server

`line-corporation/clip-japanese-base` を Hugging Face Transformers 経由でロードし、
テキスト/画像の埋め込みベクトルを返すだけの薄い FastAPI サーバ。

バックエンド (Scala 側) から `ENV_CLIP_SERVER_URL` 経由で呼び出される。

## エンドポイント

| Method | Path     | パラメータ                     | 返り値                     |
|--------|----------|--------------------------------|----------------------------|
| GET    | `/text`  | `q` (クエリ文字列)             | `{"tensor": [float, ...]}` |
| GET    | `/image` | `file` (multipart/form-data)   | `{"tensor": [float, ...]}` |

## ローカルで動かす (Docker なし)

### 1. ツールチェイン

`.tool-versions` に Python 3.11.3 / Poetry 1.8.5 が指定されている。asdf か mise で揃える:

```shell
mise install      # または: asdf install
```

### 2. 依存関係のインストール

`pyproject.toml` の `packages = [{include = "test_clip"}]` は実体が無いため、
プロジェクト自身をインストールせず依存だけ入れる:

```shell
cd clip-server
poetry install --no-root
```

`torch` は CPU 版 (`torch-2.0.1+cpu.cxx11.abi`) を直接 URL から取得する。

### 3. モデルの事前ダウンロード (任意)

`transformers` は初回呼び出し時に自動でモデルをダウンロードするため必須ではないが、
起動直後のレスポンスを早めたければ事前に落としておける。キャッシュは
`~/.cache/huggingface/` (環境変数 `HF_HOME` でも上書き可) に置かれる。

```shell
poetry run ./download_model.sh
```

> 旧モデル用の `japanese-cloob-vit-b-16/` ディレクトリと `.gitignore` のエントリは
> `line-corporation/clip-japanese-base` への移行 (commit 17199ab) 以降は不要。

### 4. サーバを起動

開発用 (オートリロード):

```shell
poetry run uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

本番相当 (Dockerfile の `CMD` と同じ):

```shell
poetry run gunicorn -k uvicorn.workers.UvicornWorker -b 0.0.0.0:8000 main:app
```

### 5. 動作確認

```shell
curl 'http://localhost:8000/text?q=テスト'
curl -F file=@/path/to/photo.jpg http://localhost:8000/image
```

## バックエンドから繋ぐ場合

Scala バックエンドをローカル `sbt ~Jetty/start` で動かしつつ、このサーバも
ローカル直起動した場合は `.env` で:

```
ENV_CLIP_SERVER_URL=http://localhost:8000
```

を指定する (デフォルトの `docker-compose.dev.yml` は `clip-server:8000` を向いている)。
