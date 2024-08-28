#!/usr/bin/env bash

##
## COPYRIGHT (C) Ericsson 2016-2018
## 
## The copyright to the computer program(s) herein is the property of
## Ericsson Inc. The programs may be used and/or copied only with written
## permission from Ericsson Inc. or in accordance with the terms and
## conditions stipulated in the agreement/contract under which the
## program(s) have been supplied.
##

##
## Script to run APEX Applications
## Call -h for help
## - requires BASH with associative arrays, bash of at least version 4
## - for BASH examples with arrays see for instance: http://www.artificialworlds.net/blog/2012/10/17/bash-associative-array-examples/
## - adding a new app means to add a command to APEX_APP_MAP and a description to APEX_APP_DESCR_MAP using same/unique key
##
## @package    com.ericsson.apex.apex
## @author     Sven van der Meer <sven.van.der.meer@ericsson.com>
## @copyright  Ericsson
## @license    proprietary
## @version    v0.7.0


##
## DO NOT CHANGE CODE BELOW, unless you know what you are doing
##


if [ -z $APEX_HOME ]
then
	APEX_HOME="/opt/ericsson/apex/apex"
fi

if [ ! -d $APEX_HOME ]
then
	echo
	echo 'Apex directory "'$APEX_HOME'" not set or not a directory'
	echo "Please set environment for 'APEX_HOME'"
	exit
fi


## script name for output
MOD_SCRIPT_NAME=`basename $0`

## check BASH version, we need >=4 for associative arrays
if [ "${BASH_VERSION:0:1}" -lt 4 ] ; then
	echo
	echo "$MOD_SCRIPT_NAME: requires bash 4 or higher for associative arrays"
	echo
	exit
fi

## config for CP apps
_config="-Dlogback.configurationFile=$APEX_HOME/etc/logback.xml -Dhazelcast.config=$APEX_HOME/etc/hazelcast.xml -Dhazelcast.mancenter.enabled=false"

## Maven/APEX version
_version=`cat $APEX_HOME/etc/app-version.txt`


## system to get CygWin paths
system=`uname -s | cut -c1-6`
cpsep=":"
if [ "$system" == "CYGWIN" ] ; then
	APEX_HOME=`cygpath -m ${APEX_HOME}`
	cpsep=";"
fi

## CP for CP apps
CLASSPATH="$APEX_HOME/etc${cpsep}$APEX_HOME/etc/hazelcast${cpsep}$APEX_HOME/etc/infinispan${cpsep}$APEX_HOME/lib/*"


## array of applications with name=command
declare -A APEX_APP_MAP
APEX_APP_MAP["ws-console"]="java -jar $APEX_HOME/lib/applications/apex-apps.wsclients-simple-$_version-jar-with-dependencies.jar -c"
APEX_APP_MAP["ws-echo"]="java -jar $APEX_HOME/lib/applications/apex-apps.wsclients-simple-$_version-jar-with-dependencies.jar"
APEX_APP_MAP["tpl-event-json"]="java -cp ${CLASSPATH} $_config com.ericsson.apex.apps.generators.model.model2event.Application"
APEX_APP_MAP["model-2-cli"]="java -cp ${CLASSPATH} $_config com.ericsson.apex.apps.generators.model.model2cli.Application"
APEX_APP_MAP["rest-editor"]="java -Dlogback.configurationFile=$APEX_HOME/etc/logback.xml -jar $APEX_HOME/lib/applications/apex-services.client-editor-$_version-editor.jar"
APEX_APP_MAP["cli-editor"]="java -cp ${CLASSPATH} $_config com.ericsson.apex.auth.clieditor.ApexCLIEditorMain"
APEX_APP_MAP["engine"]="java -cp ${CLASSPATH} $_config com.ericsson.apex.service.engine.main.ApexMain"
APEX_APP_MAP["eng-deployment"]="java -Dlogback.configurationFile=$APEX_HOME/etc/logback.xml -jar $APEX_HOME/lib/applications/apex-services.client-deployment-$_version-deployment.jar"
APEX_APP_MAP["eng-monitoring"]="java -Dlogback.configurationFile=$APEX_HOME/etc/logback.xml -jar $APEX_HOME/lib/applications/apex-services.client-monitoring-$_version-monitoring.jar"
APEX_APP_MAP["full-client"]="java -Dlogback.configurationFile=$APEX_HOME/etc/logback.xml -jar $APEX_HOME/lib/applications/apex-services.client-full-$_version-full.jar"

## array of applications with name=description
declare -A APEX_APP_DESCR_MAP
APEX_APP_DESCR_MAP["ws-console"]="a simple console sending events to APEX, connect to APEX consumer port"
APEX_APP_DESCR_MAP["ws-echo"]="a simple echo client printing events received from APEX, connect to APEX producer port"
APEX_APP_DESCR_MAP["tpl-event-json"]="provides JSON templates for events generated from a policy model"
APEX_APP_DESCR_MAP["model-2-cli"]="generates CLI Editor Commands from a policy model"
APEX_APP_DESCR_MAP["rest-editor"]="starts the APEX REST Editor inside a simple webserver"
APEX_APP_DESCR_MAP["cli-editor"]="runs the APEX CLI Editor"
APEX_APP_DESCR_MAP["engine"]="starts the APEX engine"
APEX_APP_DESCR_MAP["eng-deployment"]="starts the APEX deployment client in a simple webserver"
APEX_APP_DESCR_MAP["eng-monitoring"]="starts the APEX engine monitoring client in a simple webserver"
APEX_APP_DESCR_MAP["full-client"]="starts the full APEX client (rest editor, deployment, monitoring) in a simple webserver"

##
## Help screen and exit condition (i.e. too few arguments)
##
Help()
{
	echo ""
	echo "$MOD_SCRIPT_NAME - runs APEX applications"
	echo ""
	echo "       Usage:  $MOD_SCRIPT_NAME [options] | [<application> [<application options>]]"
	echo ""
	echo "       Options"
	echo "         -d <app>    - describes an application"
	echo "         -l          - lists all applications supported by this script"
	echo "         -h          - this help screen"
	echo ""
	echo ""
	exit 255;
}
if [ $# -eq 0 ]; then
	Help
fi


##
## read command line, cannot do as while here due to 2-view CLI
##
if [ "$1" == "-l" ]; then
	echo "$MOD_SCRIPT_NAME: supported applications:"
	echo " --> ${!APEX_APP_MAP[@]}"
	echo ""
	exit 0
fi
if [ "$1" == "-d" ]; then
	if [ -z "$2" ]; then
		echo "$MOD_SCRIPT_NAME: no application given to describe, supported applications:"
		echo " --> ${!APEX_APP_MAP[@]}"
		echo ""
		exit 0;
	else
		_cmd=${APEX_APP_DESCR_MAP[$2]}
		if [ -z "$_cmd" ]; then
			echo "$MOD_SCRIPT_NAME: unknown application '$2'"
			echo ""
			exit 0;
		fi
		echo "$MOD_SCRIPT_NAME: application '$2'"
		echo " --> $_cmd"
		echo ""
		exit 0;
	fi
fi
if [ "$1" == "-h" ]; then
	Help
	exit 0
fi


_app=$1
shift
_cmd=${APEX_APP_MAP[$_app]}
if [ -z "$_cmd" ]; then
	echo "$MOD_SCRIPT_NAME: application '$_app' not supported"
	exit 1
fi
_cmd="$_cmd $*"
## echo "$MOD_SCRIPT_NAME: running application '$_app' with command '$_cmd'"
exec $_cmd

