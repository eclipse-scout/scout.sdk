#!/bin/bash

BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

GIT_USERNAME=
VERSION=

function usage {
  cat << EOF

	${PRG} [-h] --git_username <GitUser> --version <VERSION> --scoutrt_version <RT_VERSION> [--tag <TAG>]

	-h                                  - Usage info
	-u | --git_username <GitUser>       - Eclipse Gerrit Username, SSH Key is used for authorisation
	-v | --version <VERSION>            - <VERSION> name
	-s | --scoutrt_version <RT_VERSION> - <RT_VERSION> Release Scout Version
	-t | --tag <TAG>                    - <TAG> name (Optional / Default: <VERRSION>)

	Example: ${PRG} -u sleicht -v 10.0.7-beta.1 -s 10.0.42

EOF
}

function get_options {
	# Loop until all parameters are used up
	while [ "$1" != "" ]; do
		case $1 in
			-u | --git_username )		shift
										GIT_USERNAME=$1
										;;
			-v | --version )			shift
										VERSION=$1
										;;
			-s | --scoutrt_version )	shift
										SCOUT_RT=$1
										;;
			-t | --tag )				shift
										TAG=$1
										;;
			-h | --help )				usage
										exit 7
										;;
			* )							break;;
		esac
		shift
	done
	_MAVEN_OPTS="$_MAVEN_OPTS $@"
}
get_options $*

if [[ -z  "$GIT_USERNAME" ]]; then
	echo "[ERROR]:       <GitUser> missing"
	usage
	exit 7
fi
if [[ -z "$VERSION" ]]; then
	echo "[ERROR]:       <VERSION> missing"
	usage
	exit 7
fi
if [[ "$TAG" ]]; then
	_MAVEN_OPTS="$_MAVEN_OPTS -Dmaster_release_tagName=$TAG"
fi
_MAVEN_OPTS="$_MAVEN_OPTS -e -B"

mvn -f updatesite-maven-plugin -P release.setversion -N -Dmaster_release_newVersion=$VERSION -Dorg.eclipse.scout.rt_version=$SCOUT_RT $_MAVEN_OPTS
processError
mvn -f scout-helloworld-app -P release.setversion -N -Dmaster_release_newVersion=$VERSION -Dorg.eclipse.scout.rt_version=$SCOUT_RT $_MAVEN_OPTS
processError
mvn -f scout-jaxws-module -P release.setversion -N -Dmaster_release_newVersion=$VERSION -Dorg.eclipse.scout.rt_version=$SCOUT_RT $_MAVEN_OPTS
processError
mvn -f scout-hellojs-app -P release.setversion -N -Dmaster_release_newVersion=$VERSION -Dorg.eclipse.scout.rt_version=$SCOUT_RT $_MAVEN_OPTS
processError

mvn -f org.eclipse.scout.sdk -P release.setversion -N -Dmaster_release_newVersion=$VERSION -Dorg.eclipse.scout.rt_version=$SCOUT_RT $_MAVEN_OPTS
processError
mvn -f org.eclipse.scout.sdk clean install -U -Dmaster_unitTest_failureIgnore=false $_MAVEN_OPTS
processError

mvn -f org.eclipse.scout.sdk.p2 -P release.setversion -N -Dmaster_release_newVersion=$VERSION -Dorg.eclipse.scout.rt_version=$SCOUT_RT -Dtycho.mode=maven $_MAVEN_OPTS
processError
mvn -f org.eclipse.scout.sdk.p2 clean install -T0.5C -Dmaster_unitTest_failureIgnore=false $_MAVEN_OPTS
processError

mvn -f org.eclipse.scout.sdk -P release.checkin -Declipse_gerrit_username=$GIT_USERNAME $_MAVEN_OPTS
processError

mvn -f org.eclipse.scout.sdk -P release.tag -Declipse_gerrit_username=$GIT_USERNAME -Dmaster_release_pushChanges=true $_MAVEN_OPTS
processError

git reset HEAD~1 --hard
