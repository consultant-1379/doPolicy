::
:: COPYRIGHT (C) Ericsson 2016-2018
:: 
:: The copyright to the computer program(s) herein is the property of
:: Ericsson Inc. The programs may be used and/or copied only with written
:: permission from Ericsson Inc. or in accordance with the terms and
:: conditions stipulated in the agreement/contract under which the
:: program(s) have been supplied.
::

::
:: Script to run APEX Applications
:: Call -h for help
:: - adding a new app means to add a command to APEX_APP_MAP and a description to APEX_APP_DESCR_MAP using same/unique key
::
:: @package    com.ericsson.apex.apex
:: @author     Sven van der Meer <sven.van.der.meer@ericsson.com>
:: @copyright  Ericsson
:: @license    proprietary
:: @version    v0.7.0


::
:: DO NOT CHANGE CODE BELOW, unless you know what you are doing
::

@echo off
setlocal enableDelayedExpansion


if defined APEX_HOME (
	if exist "%APEX_HOME%\" (
		set _dummy=dir
	) else (
		echo[
		echo Apex directory 'APEX_HOME' not a directory
		echo Please set environment for 'APEX_HOME'
		echo[
		exit /b
	)
) else (
	echo[
	echo Apex directory 'APEX_HOME' not set
	echo Please set environment for 'APEX_HOME'
	echo[
	exit /b
)


:: script name for output
set MOD_SCRIPT_NAME=apexApps

:: config for CP apps
SET _CONFIG=-Dlogback.configurationFile=%APEX_HOME%\etc\logback.xml -Dhazelcast.config=%APEX_HOME%\etc\hazelcast.xml -Dhazelcast.mancenter.enabled=false

:: Maven/APEX version
set /p _VERSION=<%APEX_HOME%\etc\app-version.txt


:: CP separator
set cpsep=;


:: CP for CP apps
set CLASSPATH=%APEX_HOME%\etc%cpsep%%APEX_HOME%\etc\hazelcast%cpsep%%APEX_HOME%\etc\infinispan%cpsep%%APEX_HOME%\lib\*


:: array of applications with name=command
:: declare -A APEX_APP_MAP
set APEX_APP_MAP[ws-console]=java -jar %APEX_HOME%\lib\applications\apex-apps.wsclients-simple-%_VERSION%-jar-with-dependencies.jar -c
set APEX_APP_MAP[ws-echo]=java -jar %APEX_HOME%\lib\applications\apex-apps.wsclients-simple-%_VERSION%-jar-with-dependencies.jar
set APEX_APP_MAP[tpl-event-json]=java -cp %CLASSPATH% %_CONFIG% com.ericsson.apex.apps.generators.model.model2event.Application
set APEX_APP_MAP[model-2-cli]=java -cp %CLASSPATH% %_CONFIG% com.ericsson.apex.apps.generators.model.model2cli.Application
set APEX_APP_MAP[rest-editor]=java -Dlogback.configurationFile=%APEX_HOME%\etc\logback.xml -jar %APEX_HOME%\lib\applications\apex-services.client-editor-%_VERSION%-editor.jar
set APEX_APP_MAP[cli-editor]=java -cp %CLASSPATH% %_CONFIG% com.ericsson.apex.auth.clieditor.ApexCLIEditorMain
set APEX_APP_MAP[engine]=java -cp %CLASSPATH% %_CONFIG% com.ericsson.apex.service.engine.main.ApexMain
set APEX_APP_MAP[eng-deployment]=java -Dlogback.configurationFile=%APEX_HOME%\etc\logback.xml -jar %APEX_HOME%\lib\applications\apex-services.client-deployment-%_VERSION%-deployment.jar
set APEX_APP_MAP[eng-monitoring]=java -Dlogback.configurationFile=%APEX_HOME%\etc\logback.xml -jar %APEX_HOME%\lib\applications\apex-services.client-monitoring-%_VERSION%-monitoring.jar
set APEX_APP_MAP[full-client]=java -Dlogback.configurationFile=%APEX_HOME%\etc\logback.xml -jar %APEX_HOME%\lib\applications\apex-services.client-full-%_VERSION%-full.jar

:: array of applications with name=description
:: declare -A APEX_APP_DESCR_MAP
set APEX_APP_DESCR_MAP[ws-console]=a simple console sending events to APEX, connect to APEX consumer port
set APEX_APP_DESCR_MAP[ws-echo]=a simple echo client printing events received from APEX, connect to APEX producer port
set APEX_APP_DESCR_MAP[tpl-event-json]=provides JSON templates for events generated from a policy model
set APEX_APP_DESCR_MAP[model-2-cli]=generates CLI Editor Commands from a policy model
set APEX_APP_DESCR_MAP[rest-editor]=starts the APEX REST Editor inside a simple webserver
set APEX_APP_DESCR_MAP[cli-editor]=runs the APEX CLI Editor
set APEX_APP_DESCR_MAP[engine]=starts the APEX engine
set APEX_APP_DESCR_MAP[eng-deployment]=starts the APEX deployment client in a simple webserver
set APEX_APP_DESCR_MAP[eng-monitoring]=starts the APEX engine monitoring client in a simple webserver
set APEX_APP_DESCR_MAP[full-client]=starts the full APEX client (rest editor, deployment, monitoring) in a simple webserver


:: no command line means help, -h means help
if "%1" == "" goto Help
if "%1" == "-h" goto Help

:: -l means list
if "%1" == "-l" goto ListApps

:: -d means describe
if "%1" == "-d" goto DescribeApp


::
:: ok, we need to look for an application, should be in %1
::
set _APP=%1

set _CMD=!APEX_APP_MAP[%_APP%]!
if "!_CMD!" == "" (
	echo %MOD_SCRIPT_NAME%: : application '%_APP%' not supported
	echo[
	exit /b
)

for /f "tokens=1,* delims= " %%a in ("%*") do set ACTUAL_CLI=%%b
set _CMD_RUN=%_CMD% %ACTUAL_CLI%
:: echo %MOD_SCRIPT_NAME%: running application %_APP%' with command '%_CMD_RUN%'
%_CMD_RUN%
exit /b


::
:: Help screen and exit condition (i.e. too few arguments)
::
:Help
echo[
echo %MOD_SCRIPT_NAME% - runs APEX applications
echo[
echo        Usage:  %MOD_SCRIPT_NAME% [options] ^| [^<application^> [^<application options^>]]
echo[
echo        Options
echo          -d ^<app^>    - describes an application
echo          -l          - lists all applications supported by this script
echo          -h          - this help screen
echo[
echo[
exit /b



::
:: List applications
::
:ListApps
echo[
echo %MOD_SCRIPT_NAME%: supported applications:
for /F "tokens=2,3 delims=[]=" %%a in ('set APEX_APP_MAP') do (
	echo --^> %%a
)
echo[
exit /b


::
:: Describe an application
::
:DescribeApp
if "%2" == "" (
	echo %MOD_SCRIPT_NAME%: : supported applications:
	for /F "tokens=2,3 delims=[]=" %%a in ('set APEX_APP_MAP') do (
		echo --^> %%a
	)
	echo[
	exit /b
)
set _CMD=!APEX_APP_DESCR_MAP[%2%]!
if "%_CMD%" == "" (
	echo %MOD_SCRIPT_NAME%: : unknown application '%2%'
	echo[
	exit /b
)
echo %MOD_SCRIPT_NAME%: : application '%2%'
echo --^> %_CMD%
echo[
exit /b
