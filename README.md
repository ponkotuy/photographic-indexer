# Photographic Indexer

## Setup
```shell
$ git clone git@github.com:ponkotuy/photographic-indexer.git
$ cd photographic-indexer/docker/flyway/drivers
$ ./download_mysql.sh
```

## Local Run
```shell
$ cd path/to/photographic-indexer
$ docker-compose up -d
```

## Development
### Backend(Scala+Scalatra)
```shell
$ docker-compose up db # DB
$ sbt ~container:start
```

If you need migration,

```shell
$ docker-compose up flyway db
```

### Frontend(SvelteKit)
```shell
$ npm run dev -- --open
```

### Release
Rewrite the version of build.sbt, commit and release on GitHub. Docker images are pushed to DockerHub by GitHub Actions.
