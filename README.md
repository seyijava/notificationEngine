

High-Porformce Notification Engine in ZIO Scala that feeds of data from Apache Kafka to send SMS,EMAIL or connects via webhook.

[![Build Status](https://travis-ci.org/ziyasal/Reserveon.svg?branch=master)](https://travis-ci.org/ziyasal/Reserveon)

## Tech stack
 - [Scala](https://www.scala-lang.org/)
 - [ZIO](https://zio.dev/next/overview/)
 - [ZIO Kafka](https://github.com/zio/zio-kafka)
 - [ZIO Http](https://github.com/dream11/zio-http)
 - [ZIO json](https://github.com/zio/zio-json)
 - [Apache Common Mail](https://commons.apache.org/proper/commons-email)


## Implemented Features:
- OAuth2 support (client_credentials, password, refresh_token flows)
- CORS support
- Movie & Reservation CRUD
- Simple reservation mechanism using Redis ( _without distributed lock mechanism see: **[Improvements](#improvements--todo)** section_ )
- Database schema migration
- Route tests

## Commands
### Run
:warning: _If you want to use **`docker-compose`**, you can skip manual steps._

#### Setup Postgres Database using Docker
**Run Postgres container**  
_It will create database and user_
```sh
docker run --name pg-reserveon -itd --restart always \
  --publish 65432:5432 \
  --env 'PG_PASSWORD=passw0rd' \
  --env 'DB_USER=reserveonUser' --env 'DB_PASS=s3cret'  --env 'DB_NAME=reserveon' \
  sameersbn/postgresql:9.6-2
```

#### Setup Redis using Docker
**To Run redis container;**  
```sh
docker run --name redis-reserveon -d -p 6379:6379 redis
```

**Environment variables**  
- `DB_PG_URL`  - db url by scheme jdbc:postgresql://host:port/db  
- `DB_PG_USER` - db user  
- `DB_PG_PWD`  - db password  
- `DB_CREATE_SAMPLE_DATA`  - enable or disable to create sample data (credential, token etc)  
- `REDIS_HOST`  - redis host  
- `REDIS_PORT`  - redis port  

**_Sample run command_**
```sh
DB_PG_URL=jdbc:postgresql://localhost:65432/reserveon \
DB_PG_USER=reserveonUser \
DB_PG_PWD=s3cret \
DB_CREATE_SAMPLE_DATA=true \
REDIS_HOST=localhost \
REDIS_PORT=6379
sbt run
```

**_Sample run command using docker `link` to `posgtres` and `redis` using local application image_**
```sh
docker run --name api \
-e DB_USER=reserveonUser -e DB_PASSWORD=s3cret \
-e DB_NAME=reserveon  -e DB_CREATE_SAMPLE_DATA=true \
--link pg-reserveon:database \
--link redis-reserveon:redis \
-d -p 9001:9001 ziyasal/reserveon
```

**OR**

Run `docker-compose`, it will start `api`, `redis` and `postgres` and will expose api port to host.  
```sh
docker-compose up
```

### Create executable
```sh
sbt packageBin
```

### Test
```sh
sbt test
```

**P.S** [**Postman Rest Client**](https://www.getpostman.com/) collections also provided for manual testing in `postman` folder.

### Coverage
```sh
sbt clean coverage test
```

**To create coverage report**
```sh
sbt coverageReport
```

## Improvements / TODO
 - Store refresh tokens in cache (e.g redis) to decrease load on DB
 - Produce better error/validation messages from API
 - API Documentation using swagger or similar tool/lib
 - Persist reservation data to DB to use for reports etc later on (currently stored in Redis)
 - Serve data as paged and implement data caching (implement cache invalidation etc)
 - Implement distributed locking mechanism for reservations (zookeeper, redis, custom etc)
 - Integration Testing
   1. DB integration tests using [`embedded postgres`](https://github.com/yandex-qatools/postgresql-embedded) or similar tool/lib
   2. Redis integration tests using [`embedded redis`](https://github.com/kstyrc/embedded-redis) or similar tool/lib
 - Use JWT authentication protocol with OAuth2 authentication framework
 - Implement account CRUD routes (logic currently implemented and 
    sample data seed uses it to create test user/s on startup if `DB_CREATE_SAMPLE_DATA` is `true`

ziλasal.
