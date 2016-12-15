That project is a **Work In Progress**

# Current State

At the moment only Twitter is supported.

### Score updating

Every 12 hours the kafka topic used as a sink for the tweets changes to let the ones already retrieved pending until their score is updated. 

The score of each tweet is updated after 11 hours to let the time to people to retweet/favorite it. 

# Article Streamer

The idea of this project is to provide a tool that will aggregate articles about one or several provided subjects (Scala, Spark...) 
from various sources (Twitter, Linkedin, Blogs... ). 
For each a score will be calculated. That score will represent the potential interest or value based on their popularity and other factors (to be determined).

## Architecture

Soon coming

## Configuration

There is two possible way of configuration :

1. Setting environment variables, which will override any other value (currently only the twitter authentication data and kafka can be configured by that mean)
  - TW_CONS_KEY  : twitter consumer key
  - TW_CONS_SEC  : twitter consumer secret
  - TW_ACC_TOKEN : twitter access token
  - TW_ACC_SEC   : twitter access token secret
  - KAFKA_BROKERS, KAFKA_CA, KAFKA_CERT, KAFKA_PRIVATE_KEY for kafka and SSL config
  - ...
 
2. The configuration file _application.conf_ contains all other and default configurations
  - Twitter authentication
  - Kakfa (including SSL certificates)
  - Spark
  - Mysql
    
### Tweaking the config
    
  If you need to modify the config beyond the environment variables provided, I highly recommend to avoid modifying _application.conf_, and instead to add a local file with your modifications and all the values you want to override ( typically _development.conf_ ).
  
  This file must be located in the same repository as _application.conf_ and must contain ```include application``` as first line. You also have to add ```-Dconfig.resource=/development.conf``` as an argument to SBT when running the app.
  
  For more information on how to override the configuration you can look at [Typesafe Config](https://github.com/typesafehub/config) which is used in this project. 
 

## Deploy and run the Aggregator on Heroku

Deploy to heroku

```$ sbt assembly deployHeroku```

Start the Aggregator in a worker

```$ heroku ps:scale worker=1```

Verify if it works

```$ heroku logs```

Stop the Aggregator

```$ heroku ps:scale worker=0``` 

## Docker

One of the options to run this system is Via Docker containers. Here are a couple of directions to help you install it by that mean.

### Docker Compose

The provided _docker-compose.yml_ file will start the aggregator, kafka and zookeeper all at once in separate containers.
```
$ docker-compose up -d
```
To see the logs
```
$ docker-compose logs -f
```
Stop all at once
```
$ docker-compose stop -t 60
```

### Kakfa

This project is design to work with Kafka 10 (v0.10.1.0). 
You can either use your own installation or docker images such as [jplock/zookeeper](https://hub.docker.com/r/jplock/zookeeper/) 
for Zookeeper and [ches/kafka](https://hub.docker.com/r/ches/kafka/)
```
$ docker run -d -p 2181:2181 --name zookeeper jplock/zookeeper:3.4.6
$ docker run -d -p 9092:9092 --name kafka --link zookeeper:zookeeper ches/kafka:0.10.1.0
```

### Aggregator

To build the docker image you first need to fill the environment variable file **docker-env.list**, 
then run the following commands from the root
```
// Build a Fatjar in ./docker
$ sbt "project aggregator" coverageOff assembly
// Build the image
$ docker build -t [dockerhub-username]/[project] ./aggregator/docker/.
```

### Twitter Score Updater

```
// Build a Fatjar in ./docker
$ sbt "project twitterScoreUpdater" coverageOff assembly
// Build the image
$ docker build -t [dockerhub-username]/[project] ./twitter-score-updater/docker/.
```

# Testing

To run the tests and generate the coverage reports (./[PROJECT]/target/scala-2.11/scoverage-report/index.html)


Aggregator
```
$ sbt test-agg
```

Twitter score updater
```
$ sbt test-score
```

Processor
```
$ sbt test-proc
```

Shared entities
```
$ sbt test-shared
```

All at once
```
$ sbt test-all
```

# Viewer 
 
 Allows to visualize the pending/accepted/rejected articles. The app is composed of the modules _webapp-backend_ and _webapp-frontend_ .
 
 ## Server
 
 Uses Hapi and Sequelize
 
 ### Configuration
 The configuration for the app host/port and database access is in _config/default.json_ , you can override it by adding a _local.json_ in the same directory 
 
 ### Install and run
 ```
 $ cd webapp-backend
 $ npm install
 $ npm start
 ```
 
 If all goes well you should see this output, indicating that the server started and Sequelize managed to connect :
 ```
 Executing (default): SELECT 1+1 AS result
 ```
 Every change will hot reload all resources automatically
   
 ## Web UI
 
 Based on Vuejs and Webpack
  
  ```
  $ cd webapp-frontend
  $ npm install
  $ npm run dev
  ```
  You should see an output similar to :
  ```
  Listening at http://localhost:8080
  ```
  Every change will hot reload all resources automatically