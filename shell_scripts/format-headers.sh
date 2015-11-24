#!/bin/bash
# Set correct copyright headers

mvn license:format -f org.eclipse.scout.sdk $*
mvn license:format -f org.eclipse.scout.sdk.p2 $*