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
The use of asdf is recommended. (Enable JDK/nodejs)

### Backend(Scala+Scalatra)
```shell
$ docker compose -f docker-compose.dev.yml up # up DB
$ sbt ~Jetty/start
```

If you need migration,

```shell
$ docker compose -f docker-compose.dev.yml --profile migration up
```

### Frontend(SvelteKit)
```shell
$ npm run dev -- --open
```

### Release
Rewrite the version of build.sbt, commit and release on GitHub. Docker images are pushed to DockerHub by GitHub Actions.
