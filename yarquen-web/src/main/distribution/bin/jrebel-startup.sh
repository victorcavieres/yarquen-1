#!/bin/bash
export JAVA_OPTS="-javaagent:$JREBEL_HOME/jrebel.jar -Drebel.log=true -Drebel.spring_data_plugin=true $JAVA_OPTS"
`dirname $0`/startup.sh $@

