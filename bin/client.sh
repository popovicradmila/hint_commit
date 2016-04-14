#!/bin/sh

### Script to run a mock client
### NB: clients should run via YCSB

NETTY_PORT_BASE=17000

# prepare to run
export CLASSPATH="HintCommitYCSBClient/target/dependency/*:HintCommitYCSBClient/target/hint-commit-client-1.0-SNAPSHOT.jar:$CLASSPATH"

# java hintcommit.driver.YcsbAdapter
java com.yahoo.ycsb.CommandLine -db hintcommit.driver.YcsbAdapter
