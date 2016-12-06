
FROM openjdk:8-jre

MAINTAINER Firens <https://github.com/firens>

#USER

#HEALTHCHECK

LABEL description="The aggregator for the Article Streamer project" \
      git.repo="https://github.com/firens/article-streamer" \
	  project.name="article-streamer"

ENV AGG_VERSION 0.0.1
ENV SCALA_VERSION 2.11.8
ENV SBT_VERSION 0.13.9

#EXPOSE 8080

# Install Scala
RUN \
  curl -fsL http://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb http://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

COPY aggregator-assembly-${AGG_VERSION}.jar /

ENTRYPOINT java -jar aggregator-assembly-${AGG_VERSION}.jar