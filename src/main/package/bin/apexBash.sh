#!/bin/bash

##
## COPYRIGHT (C) Ericsson 2016-2018
## 
## The copyright to the computer program(s) herein is the property of
## Ericsson Inc. The programs may be used and/or copied only with written
## permission from Ericsson Inc. or in accordance with the terms and
## conditions stipulated in the agreement/contract under which the
## program(s) have been supplied.
##

# Run from the Apex home directory 
if [ ! -d /home/apexuser ]
then
    echo Apex user home directory "/home/apexuser" not found
    exit
fi

# Run the command as "apexuser"
cd /home/apexuser
su apexuser