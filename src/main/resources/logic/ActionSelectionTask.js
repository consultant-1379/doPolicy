var returnValueType = Java.type("java.lang.Boolean");
var returnValue = new returnValueType(true);

load("nashorn:mozilla_compat.js");
importClass(java.util.ArrayList);

var logger = executor.logger;
var taskOutputEvent = executor.outFields;
var taskInputEvent = executor.inFields;
var primaryActionTask = executor.subject.getTaskKey("PrimaryActionTask");
var alternativeActionTask = executor.subject.getTaskKey("AlternativeActionTask");
var logOnlyActionTask = executor.subject.getTaskKey("LogOnlyActionTask");
var context = executor.getContextAlbum("ruleContext");
var historyContext = executor.getContextAlbum("historyContext");
var currentDateInUTC = new Date(Date.now()).toUTCString();
var currentDateObject = new Date(currentDateInUTC).getTime();

var rulesFromState = taskInputEvent.get("rules");
var triggerAsJSON = mapToJsonObject(taskInputEvent.entrySet());

var ruleID = rulesFromState[0];
logger.info("Executing with rule: " + ruleID);
var historyContextID = taskInputEvent.get("faultAssetId");

var rule = context.get(ruleID);
var history = historyContext.get(historyContextID);

var timeLapse = rule.get("timeLapse");
var subsequentActionWaitTime = 0;
takeActionOnTrigger();

function takeActionOnTrigger(){
    if (history != null) {
        setSubsequentActionWaitTime();
        logger.info("### Action History Context Found for: " + historyContextID + " ###");
        var previousActionTimestamp = new Date(history.get("actionTimestamp")).getTime();
        var durationSincePreviousAction = durationSince(previousActionTimestamp);
        if (subsequentActionWaitTime < durationSincePreviousAction) {
            logger.info("### Subsequent Action Wait Time HAS Passed ###");
            decideOnActionForTrigger();
        } else {
            logger.info("### Subsequent Action Wait Time HAS NOT Passed ###");
            logger.info("### Issuing LOG ONLY Action ###");
            logFilteredTrigger();
        }
    } else {
        logger.info("### No Action History Context Found for : " + historyContextID + " ###");
        if(!actionIsLogOnly(rule.get("primaryAction"))){
            primaryActionTask.copyTo(executor.selectedTask);
        }else{
            logOnlyActionTask.copyTo(executor.selectedTask);
        }
    }
}

function decideOnActionForTrigger(){
    if (!ruleHasAlternativeAction()) {
        if(!actionIsLogOnly(rule.get("primaryAction"))){
            primaryActionTask.copyTo(executor.selectedTask);
        }else{
            logOnlyActionTask.copyTo(executor.selectedTask);
        }
    } else {
        if(actionIsLogOnly(rule.get("alternativeAction"))){
            logOnlyActionTask.copyTo(executor.selectedTask);
        }else{
            var counter = getNumberOfActionsWithinTimeLapse();
            var ruleCount = rule.get("actionCount");
            if (counter >= ruleCount) {
                logger.info("### Action Count HAS Been Exceeded ###");
                alternativeActionTask.copyTo(executor.selectedTask);
            } else {
                logger.info("### Action Count HAS NOT Been Exceeded ###");
                primaryActionTask.copyTo(executor.selectedTask);
            }
        }
    }    
}

function logFilteredTrigger(){
    executionLogDataObject = initExecutionLogObject("Filtered", "Filtered because the subsequent action wait time had not passed.");
    logger.info("\n\n:::::::::::::: EXECUTION LOG OUTPUT ::::::::::::::\n\n");
    logger.info(JSON.stringify(executionLogDataObject));
    logger.info("\n\n::::::::::::::::::::::::::::::::::::::::::::::::::\n\n");
    sendRESTRequestToNBI(executionLogDataObject);
    returnValue = executor.FALSE;
}

function ruleHasAlternativeAction() {
    var alternativeAction = rule.get("alternativeAction");
    if (alternativeAction != "null") {
        logger.info("### The Rule HAS Got An Alternative Action ###");
        return true;
    } else {
        logger.info("### The Rule HAS NOT Got An Alternative Action ###");
        return false;
    }
}

function getNumberOfActionsWithinTimeLapse() {
    var arrayOfTimestamps = JSON.parse(history.get("arrayActionTimestamps"));
    var actionsWithinTimeLapse = arrayOfTimestamps.filter(filterTimestampsOutsideTimeLapse);
    updateHistoryContext(actionsWithinTimeLapse);
    return actionsWithinTimeLapse.length;
}

function filterTimestampsOutsideTimeLapse(value, index, array) {
    var arrayEntryTimestamp = new Date(value).getTime();
    var duration = durationSince(arrayEntryTimestamp);
    return duration < timeLapse;
}

function updateHistoryContext(timeLapseArray){
    var newHistoryContextObject = executor.getContextAlbum("historyContext").getSchemaHelper().createNewInstance();
    newHistoryContextObject.put("eventType", taskInputEvent.get("eventType"));
    newHistoryContextObject.put("faultAssetType", taskInputEvent.get("faultAssetType"));
    newHistoryContextObject.put("faultAssetName", taskInputEvent.get("faultAssetName"));
    newHistoryContextObject.put("action", history.get("action"));
    newHistoryContextObject.put("actionTimestamp", history.get("actionTimestamp"));
    newHistoryContextObject.put("arrayActionTimestamps", JSON.stringify(timeLapseArray));
    historyContext.put(historyContextID, newHistoryContextObject);
}

function durationSince(dateTimeObject) {
    var toMinutes = 1000 * 60;
    return ((currentDateObject - dateTimeObject) / toMinutes);
}

function setSubsequentActionWaitTime(){
    // subsequentActionWaitTime specified in minutes.
    subsequentActionWaitTime = 2;
}

function actionIsLogOnly(ruleAction){
    if(ruleAction=="logOnly"){
        return true;
    }
    return false;
}