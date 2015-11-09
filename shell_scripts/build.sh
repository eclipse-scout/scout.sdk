#!/usr/local/bin/bash
BASEDIR=$(dirname $0)
. $BASEDIR/_functions.sh

function usage {
  cat << EOF

	${PRG} [-h] -r <RELEASE>

	-h				- Usage info

	Example: ${PRG} -f <module>/pom.xml

EOF
}

function get_options {
	# Loop until all parameters are used up
	while [ "$1" != "" ]; do
		case $1 in
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

_MAVEN_OPTS="$_MAVEN_OPTS -e -B"

# Parallel executions of tests.
# Half of CPU core are used in to keep other half for OS and other programs.
mvn clean install -e -B -f org.eclipse.scout.sdk $_MAVEN_OPTS
processError

# Parallel executions of maven modules
# Half of CPU core are used in to keep other half for OS and other programs.
mvn clean install -e -B -T0.5C -f org.eclipse.scout.sdk.p2 $_MAVEN_OPTS