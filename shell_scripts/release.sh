#!/usr/local/bin/bash
BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

GIT_USERNAME=
RELEASE="TEST_RELEASE"
TAG=

function usage {
  cat << EOF

	${PRG} [-h] --git_username <EGerritUser> -r <RELEASE>

	-h                                - Usage info
	-u | --git_username <EGerritUser> - Eclipse Gerrit Username of Commiter, SSH Key is used for authorisation
	-u | --git_username <RELEASE>     - <RELEASE> name (Optional / Default: TEST_RELEASE)
	-t | --tag <TAG>                  - <TAG> name (Optional / Default: Project Version)

	Example: ${PRG} -r NIGHTLY

EOF
}

function get_options {
	# Loop until all parameters are used up
	while [ "$1" != "" ]; do
		case $1 in
			-u | --git_username )		shift
										GIT_USERNAME=$1
										;;
			-r | --release )			shift
										RELEASE=$1
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
	echo "[ERROR]:       <EGerritUser> missing"
	usage
	exit 7
fi
if [[ "$TAG" ]]; then
	_MAVEN_OPTS="$_MAVEN_OPTS -Dmaster_release_tagName=$TAG"
fi
_MAVEN_OPTS="$_MAVEN_OPTS -e -B"

mvn -Prelease.setversion -Dmaster_release_milestoneVersion=$RELEASE -Dorg.eclipse.scout.rt_version=$SCOUT_RT -f org.eclipse.scout.sdk -N $_MAVEN_OPTS
processError
mvn -Prelease.setversion -Dmaster_release_milestoneVersion=$RELEASE -Dorg.eclipse.scout.rt_version=$SCOUT_RT -f scout-helloworld-app -N $_MAVEN_OPTS
processError
mvn clean install -U -f org.eclipse.scout.sdk -Dmaster_unitTest_failureIgnore=false $_MAVEN_OPTS
processError

mvn -Prelease.setversion -Dmaster_release_milestoneVersion=$RELEASE -Dorg.eclipse.scout.rt_version=$SCOUT_RT -f org.eclipse.scout.sdk.p2 -Dtycho.mode=maven -N $_MAVEN_OPTS
processError
mvn clean install -T0.5C -f org.eclipse.scout.sdk.p2 -Dmaster_unitTest_failureIgnore=false $_MAVEN_OPTS
processError

mvn -Prelease.checkin -Declipse_gerrit_username=$GIT_USERNAME -f org.eclipse.scout.sdk $_MAVEN_OPTS
processError

mvn -Prelease.tag -Declipse_gerrit_username=$GIT_USERNAME -f org.eclipse.scout.sdk $_MAVEN_OPTS
processError

git reset HEAD~1 --hard