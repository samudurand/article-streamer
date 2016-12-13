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

# Give execution rights on the cron job
RUN chmod 0644 /etc/cron.d/pull-tweets && \
    touch /var/log/cron.log && \
    touch /var/log/updater.log

VOLUME /var/log/

# Run the command on container startup
CMD cron && tail -f /var/log/updater.log