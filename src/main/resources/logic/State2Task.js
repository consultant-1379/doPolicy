/*
 * State 2 Task
 * 
 * This task filters rules that apply to an incoming trigger based on tenantName - this must be an exact match.
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
var ruleContext = executor.getContextAlbum("ruleContext");
var triggerAsJSON = mapToJsonObject(taskInputEvent.entrySet());

//Passing the trigger along the pipeline
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

var applicableRules = executor.subject.getOutFieldSchemaHelper("rules").createNewInstance();
var rules = taskInputEvent.get("rules");
var inputTenantName = taskInputEvent.get("tenantName");
var eventType = taskInputEvent.get("eventType");

var commHostIndex = rules.indexOf("COMM-Host");
getApplicableRules();
initRuleOutput();

function getApplicableRules(){
    if(commHostIndex != -1 && eventType == "COMM")
    {
        applicableRules.add(rules[0]);
    }else{
        rules.forEach(function(i){
            var ruleTenantName = getRuleTenantName(i);
            if(inputTenantName == ruleTenantName){
                 applicableRules.add(i);
            }
        });
    }
}

function getRuleTenantName(ruleId){
    var rule = ruleContext.get(ruleId);
    return rule.get("tenantName");
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
        logger.info("Rules applied based on tenantName: "+ applicableRules.toString());
        taskOutputEvent.put("rules", applicableRules);
    }
}