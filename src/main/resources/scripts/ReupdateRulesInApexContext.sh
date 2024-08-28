#!/bin/bash

ACTIVE_RULES=$( curl -X GET http://policynbi:8080/sd/v1.0/decision-active)
echo "ACTIVE_RULES: " $ACTIVE_RULES

POST_RESPONSE=$(curl --header "Content-Type: application/json" --request POST --data $ACTIVE_RULES http://dopolicy:12346/apex/FirstConsumer/EventIn/)

echo "POST_RESPONSE = " $POST_RESPONSE
