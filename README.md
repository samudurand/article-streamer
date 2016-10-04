That project is a **Work In Progress**

# Article Streamer

The idea of this project is to provide a tool that will aggregate articles about one or several provided subjects (Scala, Spark...) 
from various sources (Twitter, Linkedin, Blogs... ). 
For each a score will be calculated. That score will represent the potential interest or value based on their popularity and other factors (to be determined).

## Configuration

There is two possible way of configuration :

1. Setting environment variables, which will override any other value (currently only the twitter authentication data can be configured by that mean)
  - TW_CONS_KEY  : twitter consumer key
  - TW_CONS_SEC  : twitter consumer secret
  - TW_ACC_TOKEN : twitter access token
  - TW_ACC_SEC   : twitter access token secret
 
2. The configuration file _application.conf_ contains all other and default configurations
  - Twitter authentication
  - Kakfa
  - Spark
    
### Tweaking the config
    
  If you need to modify the config beyond the environment variables provided, I highly recommend to avoid modifying _application.conf_, and instead to add a local file with your modifications and all the values you want to override ( typically _develop.conf_ ).
  
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

