/*
 * State 3 Task
 * 
 * This task filters rules that apply to an incoming trigger based on meterName. 
 * meters with '*' include all meterNames starting with the string that precedes the '*' - cpu* rules apply to cpu_util triggers. 
 * Only rules that apply will be passed on to the next state - if no rules apply execution will be stopped.
 * 
 */
var returnValueType = Java.type("java.lang.Boolean");
var returnValue = new returnValueType(true);

load("nashorn:mozilla_compat.js");
importClass(java.util.ArrayList);

var logger = executor.logger;
var taskOutputEvent = executor.outFields;
var taskInputEvent = executor.inFields;
var context = executor.getContextAlbum("ruleContext");
var triggerAsJSON = mapToJsonObject(taskInputEvent.entrySet());

// Passing the trigger along the pipeline
taskOutputEvent.put("name", taskInputEvent.get("name"));
taskOutputEvent.put("nameSpace", taskInputEvent.get("nameSpace"));
taskOutputEvent.put("version", taskInputEvent.get("version"));
taskOutputEvent.put("source", taskInputEvent.get("source"));
taskOutputEvent.put("target", taskInputEvent.get("target"));
taskOutputEvent.put("eventTimeStamp", taskInputEvent.get("eventTimeStamp"));
taskOutputEvent.put("eventType", taskInputEvent.get("eventType"));
taskOutputEvent.put("faultAssetType", taskInputEvent.get("faultAssetType"));
taskOutputEvent.put("faultAssetName", taskInputEvent.get("faultAssetName"));
taskOutputEvent.put("faultAssetId", taskInputEvent.get("faultAssetId"));
taskOutputEvent.put("severity", taskInputEvent.get("severity"));
taskOutputEvent.put("probableCause", taskInputEvent.get("probableCause"));
taskOutputEvent.put("additionalText", taskInputEvent.get("additionalText"));
taskOutputEvent.put("tenantName", taskInputEvent.get("tenantName"));
taskOutputEvent.put("subtenantName", taskInputEvent.get("subtenantName"));
taskOutputEvent.put("vdcName", taskInputEvent.get("vdcName"));
taskOutputEvent.put("vdcId", taskInputEvent.get("vdcId"));
taskOutputEvent.put("threshold", taskInputEvent.get("threshold"));
taskOutputEvent.put("affectedObjects", taskInputEvent.get("affectedObjects"));

var eventType = taskInputEvent.get("eventType");
var rulesObject = executor.subject.getOutFieldSchemaHelper("rules").createNewInstance();
var applicableRules = executor.subject.getOutFieldSchemaHelper("rules").createNewInstance();
var rules = taskInputEvent.get("rules");
var triggerMeterName = String(taskInputEvent.get("threshold").get(0).get("meterName"));

var commHostIndex = rules.indexOf("COMM-Host");
getApplicableRules();
initRuleOutput();

function getApplicableRules(){
    if (commHostIndex != -1 && eventType == "COMM") {
        applicableRules.add(rules[0]);
    } else {
        rules.forEach(function(i) {
            var rule = context.get(i);
            var ruleMeterName = String(rule.get("meterName"));
            var indexOfAst = ruleMeterName.indexOf("*");
            if (indexOfAst !== -1) {
                ruleMeterNameTrimmed = ruleMeterName.slice(0, -1);
                if (triggerMeterName.indexOf(ruleMeterNameTrimmed) !== -1) {
                    applicableRules.add(i);
                }
            } else {
                if (ruleMeterName.indexOf(triggerMeterName) !== -1) {
                    applicableRules.add(i);
                }
            }
        });
    }
}

function initRuleOutput(){
    if(applicableRules.length == 0){
        logger.info("### No Rule Exists For This Trigger ###");
        executionLogDataObject = initExecutionLogObject("Failed", "No rule was applied to this trigger.");
        logger.info("\n\n:::::::::::::: EXECUTION LOG OUTPUT ::::::::::::::\n\n");
        logger.info(JSON.stringify(executionLogDataObject));
        logger.info("\n\n::::::::::::::::::::::::::::::::::::::::::::::::::\n\n");
        sendRESTRequestToNBI(executionLogDataObject);
        returnValue = executor.FALSE;
    }
    else{
        logger.info("Rules applied based on meterName: "+ applicableRules.toString());
        taskOutputEvent.put("rules", applicableRules);
    }
}