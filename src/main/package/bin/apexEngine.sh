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
## Script to run the APEX
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

if [ -z $APEX_USER ]
then
	APEX_USER="apexuser"
fi

id $APEX_USER > /dev/null 2>& 1
if [ "$?" -ne "0" ]
then
	echo 'cannot run apex, user "'$APEX_USER'" does not exit'
	exit
fi

if [ $(whoami) != "$APEX_USER" ]
then
	echo 'Apex must be run as user "'$APEX_USER'"'
	exit
fi

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

if [ $(whoami) == "$APEX_USER" ]
then
	$APEX_HOME/bin/apexApps.sh engine $*
else
	su $APEX_USER -c "$APEX_HOME/bin/apexApps.sh engine $*"
fi
