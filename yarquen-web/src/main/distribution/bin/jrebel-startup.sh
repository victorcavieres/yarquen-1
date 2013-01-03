#!/bin/bash
export JAVA_OPTS="-javaagent:$JREBEL_HOME/jrebel.jar -Drebel.log=true $JAVA_OPTS"
`dirname $0`/startup.sh $@

