#!/usr/local/bin/bash
BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

# Parallel executions of tests.
# Half of CPU core are used in to keep other half for OS and other programs.
mvn clean install -e -B -DforkCount=0.5C -f org.eclipse.scout.sdk $*
processError

# Parallel executions of maven modules
# Half of CPU core are used in to keep other half for OS and other programs.
mvn clean install -e -B -T0.5C -f org.eclipse.scout.sdk.p2 $*