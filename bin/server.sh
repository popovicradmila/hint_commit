#!/bin/sh

### Script to run the server side

NETTY_PORT_BASE=17000

# prepare to run
export CLASSPATH="HintCommitYCSBServer/target/dependency/*:HintCommitYCSBServer/target/hint-commit-server-1.0-SNAPSHOT.jar:$CLASSPATH"


for id in "0" "1" "2"; do
    netty_port=$((${NETTY_PORT_BASE} + ${id}))
    PARAMS="127.0.0.1 8900 127.0.0.1 8901 127.0.0.1 8902 ${id} ${netty_port}"
    java -Dlog_fname=${id} main.java.ch.epfl.lpd.App $PARAMS &
done
