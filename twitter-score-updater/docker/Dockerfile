FROM openjdk:8-jre
MAINTAINER Firens <https://github.com/firens>

LABEL description="Tweet score updater for the Article Streamer project" \
      git.repo="https://github.com/firens/article-streamer" \
	  project.name="article-streamer"

RUN apt-get update && \
    apt-get -y install cron

ENV APP_VERSION 0.0.1

COPY score-updater-assembly-${APP_VERSION}.jar /
COPY crontab /etc/cron.d/pull-tweets

RUN chmod 0644 /etc/cron.d/pull-tweets && \
    touch /var/log/cron.log && \
    touch /var/log/updater.log

VOLUME /var/log/

CMD printenv | sed 's/^\(.*\)$/export \1/g' > /root/project_env.sh && \
    cron && exec tail -f /var/log/updater.log