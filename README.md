# Photographic Indexer

## What this
The web application to search for photos in your storage.
- Search address conversion between lat and lng
- Search shooting date
- Search from Flickr
- Set tags and notes to search

## Setup
```shell
$ git clone git@github.com:ponkotuy/photographic-indexer.git
$ cd photographic-indexer/docker/flyway/drivers
$ ./download_mysql.sh
$ cd photographic-indexer/clip-server
$ ./download_model.sh
```

## Configuration
Configure settings using environment variables. The following are supported.

- ENV_MYSQL_ROOT_PASSWORD (required)
- ENV_MYSQL_PASSWORD (required)
- ENV_DB_URL
- ENV_APP_PHOTOS_DIR (required)
- ENV_APP_EMAIL
- ENV_FLICKR_KEY
- ENV_FLICKR_SECRET

Describing them in the .env file makes them easier to handle.

## Local Run
```shell
$ cd path/to/photographic-indexer
$ docker compose up -d
```

## Local Run FlickrCrawl
After running local and setting config values, execute a following command.
```shell
$ docker compose run flickr
```

## Development
The use of asdf or mise is recommended. (Enable JDK/nodejs)

### Backend(Scala+Scalatra)
Requires `exiftool` to be installed on your system.

```shell
$ docker compose -f docker-compose.dev.yml up # up DB
$ sbt ~warStart
```

If you need migration,

```shell
$ docker compose -f docker-compose.dev.yml --profile migration up
```

### Frontend(SvelteKit)
```shell
$ npm run dev -- --open
```

### clip-server(FastAPI)
Python 3.11.3 / Poetry 1.8.5 (see `clip-server/.tool-versions`). The model is auto-downloaded by `transformers` on first request; `./download_model.sh` pre-fetches it.

> **Note**: `torch` is pinned to a cp311 wheel URL, and the rest of the lockfile only ships cp311 wheels. Using Python 3.12+ will make `poetry install` fail with "Unable to find installation candidates" / ABI tag mismatch. Make sure mise/asdf has activated 3.11 before installing.

```shell
$ cd clip-server
$ mise install                 # or: asdf install
$ poetry env use 3.11           # only if an env was already created with another Python
$ poetry install
$ poetry run dev                # alias for uvicorn with --reload; see clip_server/__init__.py
```

Point the backend at it via `ENV_CLIP_SERVER_URL=http://localhost:8000` in `.env`. See `clip-server/README.md` for endpoints and details.

### Release
Rewrite the version of build.sbt, commit and release on GitHub. Docker images are pushed to DockerHub by GitHub Actions.
