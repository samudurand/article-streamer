# article-streamer

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