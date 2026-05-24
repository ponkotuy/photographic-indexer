# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project layout

Three independently-built components, each with its own Dockerfile:

- **`src/`** — Scala 3 / Scalatra backend (the `Photographic Indexer` web API + Flickr crawler). Built with sbt.
- **`view/`** — SvelteKit frontend (`view` Docker image). Talks to the backend via SvelteKit `handleFetch`.
- **`clip-server/`** — Python FastAPI service wrapping a Japanese CLOOB-ViT-B-16 model for text/image embeddings. Built and shipped as a separate image.

Plus: `docker/flyway/sql/` (DB migrations), `docker/db/conf/` (MySQL config), `lb/` (nginx reverse-proxy config for the bundled stack).

## Common commands

### Backend (Scala / sbt)
```shell
# Requires exiftool installed on the host.
sbt ~Jetty/start           # hot-reload Jetty dev server on :8080
sbt test                   # run all tests (munit + scalatra-scalatest)
sbt "testOnly *ImageWithAllSuite"   # run a single suite
```

The DB **must** be up for the backend to start. Use `docker compose -f docker-compose.dev.yml up` (exposes MySQL on 3306 + clip-server on 8000). Run migrations with `docker compose -f docker-compose.dev.yml --profile migration up`.

Required `.env` keys for local dev (see `.env.init`): `ENV_MYSQL_ROOT_PASSWORD`, `ENV_MYSQL_PASSWORD`, `ENV_APP_PHOTOS_DIR`. Optional: `ENV_APP_EMAIL`, `ENV_FLICKR_KEY`, `ENV_FLICKR_SECRET`, `ENV_DB_URL`, `ENV_CLIP_SERVER_URL`.

### Frontend (SvelteKit)
```shell
cd view
npm run dev -- --open      # vite dev server
npm run check              # svelte-check (TS + svelte)
npm run lint               # prettier + eslint
npm run format             # prettier --write
npm run build              # production build (output goes into view/build/)
```

### Full stack
```shell
docker compose up -d                  # full local stack via the published images
docker compose run flickr             # one-shot Flickr crawl (needs FLICKR_* env)
```

### One-time setup
Before first build/run:
```shell
(cd docker/flyway/drivers && ./download_mysql.sh)   # flyway MySQL JDBC driver
(cd clip-server && ./download_model.sh)             # CLIP model weights
```

## Architecture

### Backend layout (`src/main/scala/com/ponkotuy/`)
- **`ScalatraBootstrap.scala`** (top-level package) — the single entry point. Mounts servlets and calls `Initializer.run(conf)`, which boots background workers via `CronRunner`:
  - `Indexer` (every 1h): scans `app.photos_dir` and ingests new files.
  - `CLIPIndexer` (every 1d, only if `clip` config present): computes CLIP embeddings via the FastAPI sidecar.
  - `ExifCacheBatch` (runs once, 10 min after startup): hydrates the `ExifCache` table.
  - `ImageFileChecker` (synchronous on startup): reconciles DB with the photos dir.
- **`app/`** — Scalatra servlets (`PrivateImage`, `PublicImage`, `DirectoryAPI`, `PrivateStatsAPI`, etc.). `Private*` routes are mounted under `/app/...`; `Public*` are unauthenticated equivalents under `/app/public/...`. There is **no** application-level auth — public/private is enforced by the `is_public` column.
- **`db/`** — ScalikeJDBC models. `ImageWithAll` is the central read-side aggregate: it joins `Image` with `Geom`, `ImageFile`, `ImageTag`/`Tag`, `ImageClipIndex`, `FlickrImage`, `ExifCache` in a single query, using `group_concat` to flatten the 1-to-many file/tag joins. When adding a new related table, extend `selectWithJoin` and `apply(rs)` here.
- **`batch/`** — Long-running background jobs and the `Exiftool` shell wrapper. `ExifParser` reads metadata; `ThumbnailGenerator` produces 960×640 JPEGs on demand and caches them in the `Thumbnail` table.
- **`clip/`** — `ClipAccessor` (HTTP client for clip-server) + `ClipCache` (search-time cosine similarity against `ImageClipIndex` rows).
- **`flickr/`**, **`geo/`** — HTTP clients for Flickr and Nominatim.
- **`config/`** — Typesafe Config wrappers. All config is loaded once in `MyConfig.load()` (returns `Option` so missing required keys surface as `RuntimeException("ConfigError")` at boot).
- **`req/`**, **`res/`** — Circe-encoded request/response DTOs. `SearchParams`/`SearchParamsGenerator` parses the search query string.
- **`FlickrCrawler.scala`** (top-level) — the secondary `main` for `bin/flickr-crawler` (separate Docker entrypoint).

### MySQL specifics
- **Fulltext search uses `ngram_token_size=2`** (set in `docker-compose*.yml`). When writing new fulltext indexes or queries, assume 2-char ngram tokenization.
- Schema is versioned via Flyway under `docker/flyway/sql/V*__*.sql`. To add a column/table, write a new `V<n>__...sql` file — never edit existing migrations.
- Production driver is `com.mysql:mysql-connector-j`; tests use H2 in MySQL mode (see below).

### Tests
- Framework: **munit** (`testFrameworks += new TestFramework("munit.Framework")`).
- `TestDatabase` (in `src/test/scala/com/ponkotuy/db/`) sets up an H2 file-DB (`./test_db.mv.db`) in MySQL mode and **replays the Flyway migrations through `H2SqlConverter`** to translate MySQL-specific DDL. A SHA-256 of `docker/flyway/sql/*.sql` + `TestDataInitializer.scala` is stored in `test_db.hash`; the DB file is auto-rebuilt when this hash changes.
- If you add MySQL-only SQL syntax in a migration, `H2SqlConverter` (`src/test/scala/com/ponkotuy/db/H2SqlConverter.scala`) likely needs a new rule, otherwise tests will fail at migration-apply time.
- Test data is generated by `TestDataInitializer` / `TestDataGenerator`.

### Frontend
- SvelteKit (Node adapter) + carbon-components-svelte. Routes are under `view/src/routes/` (`calendar/`, `directory/`, `image/`, `public/`, `stats/`).
- **`hooks.server.ts` rewrites all server-side `fetch` URLs to `http://web:8080`** when not in dev. This is how the SvelteKit Node server reaches the Scala backend inside the docker network. In dev, fetches go to whatever the browser/local URL is.
- Built assets are packaged **into the backend's Docker image** (`Universal / mappings ++= directory("view/build")` in `build.sbt`) — but in production `docker-compose.yml` the SvelteKit server runs as its own `view` container. Both layouts coexist.

### clip-server
FastAPI app with two endpoints: `GET /text?q=...` returns text embedding; `GET /image` (multipart upload) returns image embedding. Backend calls these via `ClipAccessor`. The model directory `japanese-cloob-vit-b-16/` is git-ignored — fetched by `download_model.sh`.

## Release flow

Tagging a GitHub release triggers `.github/workflows/release.yml`, which builds and pushes three images to **both** Docker Hub (`ponkotuy/*`) and GHCR (`ghcr.io/ponkotuy/*`): `photographic-indexer`, `photographic-indexer-view`, `clip-server`. `docker-compose.yml` pulls from GHCR. The `VERSION` env var is forwarded into sbt (`build.sbt` reads `sys.env.get("VERSION")`).

## Code style

- Scala 3 with `-no-indent -rewrite -source 3.4-migration` (braced syntax, not significant indentation). Formatted by scalafmt 3.10.1 — `maxColumn = 120`, `spaces.inImportCurlyBraces = true`, `spaces.inInterpolatedStringCurlyBraces = true`.
- Frontend formatted by Prettier + eslint (`npm run lint` / `npm run format`).
- Toolchain pinned in `.tool-versions` (Temurin 17.0.9, Node 22.12.0) — use asdf or mise.
