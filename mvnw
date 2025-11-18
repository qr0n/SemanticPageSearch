#!/bin/sh
# Maven Wrapper startup script for Unix-based systems

MAVEN_VERSION=3.9.5
MAVEN_DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip

if [ -z "$MAVEN_HOME" ]; then
  MAVEN_HOME="$HOME/.m2/wrapper/apache-maven-${MAVEN_VERSION}"
fi

if [ ! -d "$MAVEN_HOME" ]; then
  echo "Downloading Maven ${MAVEN_VERSION}..."
  mkdir -p "$HOME/.m2/wrapper"
  curl -o "$HOME/.m2/wrapper/maven.zip" "$MAVEN_DOWNLOAD_URL"
  unzip -q "$HOME/.m2/wrapper/maven.zip" -d "$HOME/.m2/wrapper"
  rm "$HOME/.m2/wrapper/maven.zip"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
