#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

# Check correct copyright headers

mvn license:check -Dlicense_check -f updatesite-maven-plugin $*
processError
mvn license:check -Dlicense_check -f org.eclipse.scout.sdk $*
processError
mvn license:check -Dlicense_check -f org.eclipse.scout.sdk.p2 -Dtycho.mode=maven $*
