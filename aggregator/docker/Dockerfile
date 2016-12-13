
FROM openjdk:8-jre

MAINTAINER Firens <https://github.com/firens>

LABEL description="The aggregator for the Article Streamer project" \
      git.repo="https://github.com/firens/article-streamer" \
	  project.name="article-streamer"

ENV AGG_VERSION 0.0.1

COPY aggregator-assembly-${AGG_VERSION}.jar /

ENTRYPOINT exec java -jar aggregator-assembly-${AGG_VERSION}.jar