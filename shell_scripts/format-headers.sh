#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

# Set correct copyright headers
mvn license:format -f updatesite-maven-plugin $*
processError
mvn license:format -f org.eclipse.scout.sdk $*
processError
mvn license:format -f org.eclipse.scout.sdk.p2 -Dtycho.mode=maven $*
