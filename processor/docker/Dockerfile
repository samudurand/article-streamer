FROM anapsix/alpine-java:8_server-jre_unlimited
MAINTAINER Firens <https://github.com/firens>

LABEL description="Article processor for the Article Streamer project" \
      git.repo="https://github.com/firens/article-streamer" \
	  project.name="article-streamer"

ENV APP_VERSION 0.0.1

COPY processor-assembly-${APP_VERSION}.jar /

ENTRYPOINT exec java -Xmx512m -jar processor-assembly-${APP_VERSION}.jar