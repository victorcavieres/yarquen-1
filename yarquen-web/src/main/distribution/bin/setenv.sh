#!/bin/sh

#logback
export JAVA_OPTS="$JAVA_OPTS -Dlogback.configurationFile=$CATALINA_HOME/conf/logback.xml"

