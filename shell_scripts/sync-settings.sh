#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

mvn org.eclipse.scout:eclipse-settings-maven-plugin:eclipse-settings -f updatesite-maven-plugin $*
processError
mvn org.eclipse.scout:eclipse-settings-maven-plugin:eclipse-settings -f org.eclipse.scout.sdk $*
processError
mvn org.eclipse.scout:eclipse-settings-maven-plugin:eclipse-settings -f org.eclipse.scout.sdk.p2 -Dtycho.mode=maven $*
