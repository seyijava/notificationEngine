

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


#### SMS

Connects via Twilio to send sms messages. To make sure your event is processed by the SMS delivery gateway when using this for a **Notification Service** the _channel_ field in your event written to Apache Kafka should be something like this:
 
 ```json                     
                      "channel": {
                        "SMS": true 
                      }   
 ```

But when using this as a **Custom Watcher**, you don't need to worry about the format since it would be formatted for you by [elasticsearch kafka watch]( https://malike.github.io/elasticsearch-kafka-watch/)

The *SMS* channel is not supported when using this for **Scheduled Reports**.

| Use as  | Supported |
| -------- |-----------|
|  Notification Service        | Yes|
|  Custom Watcher        | Yes|
|  Scheduled Reports        | No|

#### Email

Connects via SMTP to send emails. To make sure your event is processed by the Email delivery channel when using this for a **Notification Service** the _channel_ field in your event written to Apache Kafka should be something like this:

```json
    "channel": {
      "EMAIL": true
    }
```

[Elasticsearch Kafka Watch](https://malike.github.io/elasticsearch-kafka-watch/) would help use this a Custom Elasticsearch Watcher. It
would generate the right event for Apache Kafka.

For scheduled reports the same plugin would help generate the event which would cause **go-kafka-alert** to react by emailing the
report.



| Use as  | Supported |
| -------- |-----------|
|  Notification Service        | Yes|
|  Custom Watcher        | Yes|
|  Scheduled Reports        | Yes|

<br/>


#### API 


| Use as  | Supported |
| -------- |-----------|
|  Notification Service        | Yes|
|  Custom Watcher        | Yes|
|  Scheduled Reports        | No|

<br/>


**NB :** For multiple channels for the same event use this :

                      "channel": {
                        "SMS": true,
                        "EMAIL": true,
                        "API": true 
                      }

## Setup

#### Configuration 

There are two ways to load the configuration file :

**1. Spring Cloud Config**

This is a sample [Spring Cloud Config Server](https://github.com/malike/centralized-configuration-mangement/tree/master/config-server) with configruations loaded from [here](https://github.com/malike/centralized-configuration). If you want to read more on the _`whys`_ and the _`hows`_ of loading configuration files from Config Servers

[Read more](https://malike.github.io/Configuration-Management-For-Microservices-And-Distributed-Systems)

Sample configuration to start app with UAT configuration

  ```shell
      go-kafka-alert -loglevel=trace -profile=uat
  ```
  This would load the uat configuration profile `http://localhost:8888/go-kafka-alert-uat.json` from the config server.

**2. File System**

The app is meant to be a light-weight application.  Find a [sample configuration](https://github.com/malike/go-kafka-alert/blob/master/configuration.json) file:

```json

           {
             "workers": 4,
             "logFileLocation": "/var/log/go_kafka_alert.log",
             "log": true,
             "kafkaConfig": {
               "bootstrapServers": "localhost:9092",
               "kafkaTopic": "go-kafka-event-stream",
               "kafkaTopicConfig": "latest",
               "kafkaGroupId": "consumerGroupOne",
               "kafkaTimeout": 5000
             },
             "webhookConfig": {
               "appURL": "http://url.",
               "appKey": "Malike"
             },
             "smsConfig": {
               "twilioAccountId": "Malike",
               "twilioAuthToken": "Malike",
               "smsSender": "+15005550006"
             },
             "emailConfig": {
               "smtpServerHost": "smtp.gmail.com",
               "tls": true,
               "smtpServerPort": 465,
               "emailSender": "Sender",
               "emailFrom": "youreamail@gmail.com",
               "emailAuthUserName": "youreamail@gmail.com",
               "emailAuthPassword": "xxxxxx"
             },
             "dbConfig": {
               "mongoHost": "localhost",
               "mongoPort": 27017,
               "mongoDBUsername": "",
               "mongoDBPassword": "",
               "mongoDB": "go_kafka_alert",
               "collection": "message"
             },
             "templates": {
               "APPFLAG_API": "User {{.UnmappedData.UserName}} has failed to execute service {{.UnmappedData.ServiceName}} {{.UnmappedData.FailureCount}} times in the past {{.UnmappedData.FailureDuration}} minutes",
               "SERVICEHEALTH_API": "Service {{.UnmappedData.ServiceName}} has failed execution {{.UnmappedData.FailureCount}} in the past {{.UnmappedData.FailureDuration}} minutes",
               "SUBSCRIPTION_API": "Hello {{.UnmappedData.Name}}, Thanks for subscribing to {{.UnmappedData.ItemName}}",
               "APPFLAG_SMS": "User {{.UnmappedData.UserName}} has failed to execute service {{.UnmappedData.ServiceName}} {{.UnmappedData.FailureCount}} times in the past {{.UnmappedData.FailureDuration}} minutes",
               "SERVICEHEALTH_SMS": "Service {{.UnmappedData.ServiceName}} has failed execution {{.UnmappedData.FailureCount}} in the past {{.UnmappedData.FailureDuration}} minutes",
               "SUBSCRIPTION_SMS": "Hello {{.UnmappedData.Name}}, Thanks for subscribing to {{.UnmappedData.ItemName}}",
               "SUBSCRIPTION_EMAIL": "<html><head></head><body> Hello {{.UnmappedData.Name}}, Thanks for subscribing to {{.UnmappedData.ItemName}} </body></html>",
               "REPORTATTACHED_EMAIL": "<html><head></head><body> Hello {{.UnmappedData.Name}}, Find attached report for {{.UnmappedData.ItemName}} </body></html>",
               "REPORTEMBEDED_EMAIL": "{{.UnmappedData.Content}}"
             }
           }
```

<br/>

**i. kafkaConfig**
[Apache Kafka](https://kafka.apache.org/) configuration. Note you can comma separate the value for `bootstrapServers` nodes if you have multiple nodes.
Example `127.0.0.1:2181,127.0.0.2:2181`. 
For the other [Apache Kafka](https://kafka.apache.org/) configurations I'm assuming you already know how what they mean. Read the Apache Kafka docs if you want to know more. The project uses the [go kafka library](https://github.com/confluentinc/confluent-kafka-go) by Confluent.
<br/>

**ii. webhookConfig**

<br/>



**iii. smsConfig**
This is where configuration for your [twilio account](https://www.twilio.com/) are. This would enable sending SMS notifications. The project uses the [twilio sms config](https://github.com/sfreiberg/gotwilio).
<br/>

**iv. emailConfig**
This is where configuration for your _email smtp_ would be. This would enable sending EMAIL notifications. It uses [http://gopkg.in/gomail.v2](https://gopkg.in/gomail.v2)
<br/>

**v. dbConfig**
Messages sent out are stored for auditing purposes. Together with the response from twilio or your smtp gateway. This configuration stores them in MongoDB. Uses [this](https://gopkg.in/mgo.v2/bson) mongodb library for Go.
<br/>

**vi. templates**
These are the messaging templates configured for all the alert types. Follow [this](https://gohugo.io/templates/introduction/) to learn how to create your templates. The templates are stored as maps to give an *_O(1)_* when finding a template. The key of the map follows this convention `"EventType"`+`_`+`"Delivery Channel"`. This means an SMS for EventType, SUBSCRIPTION would be `SUBSCRIPTION_SMS` 
<br/>



<br/>

## Build

To compile source, clone repository and run `dep ensure`. The projects uses [dep](https://github.com/golang/dep) as a dependency management tool. 

Because of [go kafka library](https://github.com/confluentinc/confluent-kafka-go) by Confluent, you'll need to also get `librdkafka` installed. 

For Debian systems follow this [link](https://github.com/confluentinc/confluent-kafka-go/#installing-librdkafka).

For OSx use `brew install librdkafka` . 

You can use `[ldflags](https://blog.cloudflare.com/setting-go-variables-at-compile-time/)` to package the go build with the default parameters.
Required parameters are :

`profile` : Configuration profile if configuration would be loaded from  [Spring Cloud Config Server](https://github.com/malike/centralized-configuration-mangement/tree/master/config-server)

`configServer` : Base url of config server. Eg `http://localhost:8888`.

<br/>

## Download



| Version  |
| -------- |
| [0.1-Prelease Tag]()   |


## Contribute

Contributions are always welcome!
Please read the [contribution guidelines](CONTRIBUTING.md) first.

## Code of Conduct

Please read [this](CODE_OF_CONDUCT.md).

## License

[GNU General Public License v3.0](https://github.com/malike/go-kafka-alert/blob/master/LICENSE)


