#!/bin/bash
BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

export MAVEN_OPTS="$MAVEN_OPTS -Xmx256m"

echo ''
echo ''
echo '                 uuuuuuu'
echo '             uu$$$$$$$$$$$uu'
echo '          uu$$$$$$$$$$$$$$$$$uu'
echo '         u$$$$$$$$$$$$$$$$$$$$$u'
echo '        u$$$$$$$$$$$$$$$$$$$$$$$u'
echo '       u$$$$$$$$$$$$$$$$$$$$$$$$$u'
echo '       u$$$$$$$$$$$$$$$$$$$$$$$$$u'
echo '       u$$$$$$"   "$$$"   "$$$$$$u'
echo '       "$$$$"      u$u       $$$$"'
echo '        $$$u       u$u       u$$$'
echo '        $$$u      u$$$u      u$$$'
echo '         "$$$$uu$$$   $$$uu$$$$"'
echo '          "$$$$$$$"   "$$$$$$$"'
echo '            u$$$$$$$u$$$$$$$u'
echo '             u$"$"$"$"$"$"$u'
echo '  uuu        $$u$ $ $ $ $u$$       uuu'
echo ' u$$$$        $$$$$u$u$u$$$       u$$$$'
echo '  $$$$$uu      "$$$$$$$$$"     uu$$$$$$'
echo 'u$$$$$$$$$$$uu    """""    uuuu$$$$$$$$$$'
echo '$$$$"""$$$$$$$$$$uuu   uu$$$$$$$$$"""$$$"'
echo ' """      ""$$$$$$$$$$$uu ""$"""'
echo '           uuuu ""$$$$$$$$$$uuu'
echo '  u$$$uuu$$$$$$$$$uu ""$$$$$$$$$$$uuu$$$'
echo '  $$$$$$$$$$""""           ""$$$$$$$$$$$"'
echo '   "$$$$$"                      ""$$$$""'
echo '     $$$"                         $$$$"'
echo ''
echo ''
echo '           TESTS ARE DISABLED'
echo '               ARE U CRAZY ?'
echo ''
echo ''
echo ''

mvn clean install -B -e -DskipTests=true -Pdev $*
processError
mvn clean install -Dp2 -B -e -T0.5C -DskipTests=true -Pdev $*