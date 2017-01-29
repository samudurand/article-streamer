# Aggregator

At the moment the Aggregator only gets his data from Twitter with a single focus : recovering tweets referring to articles or other learning resources about Apache Spark. 
The goal is to eventually aggregates articles from several sources such as (Professional) social networks (Linkedin, Twitter), blogs, and any other potential article source.

## Architecture

Coming soon

## Process

Only Twitter is currently supported. Using [Twitter4J]() the aggregator streams all tweets containing the tag *#Spark* and apply the following steps to each tweet : 

Need to replace by a schema 
1. Is retweet ?
2. Is in English ?
3. Is from an ignored Author
4. Contains at least one usable link* (not a link to Twitter, another social medias, an unusable media (image, gif...) or a domain intentionally ignored)
5. At least one of the usable URL is unknown
6. Memorize the new URL(s) into **Redis**
7. Is it morning ? 
8. Send the tweet to the morning **Kafka** topic / Send the tweet to the afternoon Kafka topic

## Development

For development purpose you will need a local configuration file such as "development.conf". 
Place it beside the original configuration file in *shared/src/main/resources* or inside the aggregator module directly *aggregator/src/main/resources*.
This first line has to be :

```
include "application"
```

Any configuration specified in *application.conf* can be overridden by specifying it in this new file.

To run the application add to the command line ```-Dconfig.resource=/development.conf ```

The following sections describe the minimal data that you need to provide.

#### Twitter

You need a valid Twitter Apps with keys and token set up. See [Twitter's Apps page](https://apps.twitter.com) to create your own. 
In the local configuration file set the following object :

```
twitter {
  oauth {
    oauthConsumerKey = "your key"
    oauthConsumerSecret = "your secret"
    oauthAccessToken = "your token"
    oauthAccessTokenSecret = "your token secret"
  }
}
```

### Kafka

At the minimum you have to add the following object to your local configuration file with your own Kafka details. 

```
kafka {
  brokers = "YOUR_KAFKA_HOST:YOUR_KAFKA_PORT"
}
```

### Redis

By default Redis is assumed to be running on localhost on the default port 6379. 
Any URL stored as a key in Redis will by default be deleted after a month. 

Any of those can be changed by adding any of those to the local config file : 

```
redis {
  host: "localhost"
  port: 6379
  expiryTime: 2678000
}
```