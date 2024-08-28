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
:: Script to run the APEX
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

%APEX_HOME%\bin\apexApps.bat engine %*
