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
4. Contains at least one usable link* (not a link to Twitter, another social medias, an unusable media (image, gif...))

## Development

For development purpose you will need a local configuration file such as "development.conf".
Use it to override the default configuration file "application.conf" by placing the new file in the same folder and writing as a first line 

```
include "application"
```

To run the application add to the command line ```-Dconfig.resource=/development.conf ```

#### Twitter

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

## Kafka

TODO