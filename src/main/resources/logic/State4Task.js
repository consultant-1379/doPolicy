/*
 * State 4 Task
 * 
 * This task filters rules that apply to an incoming trigger based on severity and severityEvaluation
 * Only rules that apply will be passed on to the next state - if no rules apply execution will be stopped.
 * 
 * Rule Priority Evaluation:
 * 
 *  1. Direct Equals - trigger: Major & rule: Equal_to_Major
 *  2. Greater than or equals rules with a matching severity - trigger: Major & rule: Greater_Than_Equal_to_Major
 *  3. Greater than rules with closest severity - trigger: Critical & rule: Greater_Than_Critical
 *  4. Greater than or equals rules - trigger: Major & rule: Greater_Than_Equal_to_Minor
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

var rules = taskInputEvent.get("rules");

var equalsRuleApplied = [];
var greaterThanEqualsRuleWithMatchingSeverity = [];
var greaterThanRules = [];
var greaterThanEqualsRule = [];

var applicableRuleObject = {};
var applicableRules = [];

var alarmBandsPriority = ["minor", "major", "critical"];
var eventType = taskInputEvent.get("eventType");
var commHostIndex = rules.indexOf("COMM-Host");
if (commHostIndex != -1 && eventType == "COMM") {
    var id = rules[0];
    outputAppliedRule(id);
} else {
    rules.forEach(function(i){
        applySeverityEvaluation(i);
    });
    applyRule();
}

function applySeverityEvaluation(ruleId){
    var rule = context.get(ruleId);
    var ruleSeverity = rule.get("severity");
    var ruleSeverityEval = rule.get("severityEvaluation");
    var triggerSeverity = taskInputEvent.get("severity");
    var ruleAppliesBool = checkEvaluationApplies(ruleSeverity, ruleSeverityEval, triggerSeverity);
    if(ruleAppliesBool){
        applicableRuleObject.evaluation = ruleSeverityEval;
        applicableRuleObject.triggerSeverity = triggerSeverity;
        applicableRuleObject.ruleSeverity = ruleSeverity;
        applicableRuleObject.id = ruleId;
        applicableRules.push(applicableRuleObject);
    }
}

function checkEvaluationApplies(ruleSeverity, ruleSeverityEval, triggerSeverity){
    if(equalsRuleApplies(ruleSeverity, ruleSeverityEval, triggerSeverity)){
        return true;
    }else{
        return greaterThanRuleApplies(ruleSeverity, ruleSeverityEval, triggerSeverity);
    }
}

function equalsRuleApplies(ruleSeverity, ruleSeverityEval, triggerSeverity){
    if(ruleSeverity == triggerSeverity && ruleSeverityEval == "Equal_to"){
        return true;
    }else{
        return false;
    }
}

function greaterThanRuleApplies(ruleSeverity, ruleSeverityEval, triggerSeverity){
    if(ruleSeverityEval == "Greater_Than_Equal_to" && ruleSeverity == triggerSeverity){
        return true;
    }else{
        return isEncapsulatedByRule(ruleSeverity, triggerSeverity);
    }
}

function isEncapsulatedByRule(ruleSeverity, triggerSeverity){
    var triggerSeverityIndex = alarmBandsPriority.indexOf(triggerSeverity);
    var ruleSeverityIndex = alarmBandsPriority.indexOf(String(ruleSeverity));
    if(triggerSeverityIndex > ruleSeverityIndex){
        return true;
    }else{
        return false;
    }
}

function applyRule(){
    if(applicableRules.length == 0){
        logger.info("### No Rule Exists For This Trigger ###");
        executionLogDataObject = initExecutionLogObject("Failed", "No rule was applied to this trigger.");
        logger.info("\n\n:::::::::::::: EXECUTION LOG OUTPUT ::::::::::::::\n\n");
        logger.info(JSON.stringify(executionLogDataObject));
        logger.info("\n\n::::::::::::::::::::::::::::::::::::::::::::::::::\n\n");
        sendRESTRequestToNBI(executionLogDataObject);
        returnValue = executor.FALSE;
    }else if(applicableRules.length == 1){
        var id = applicableRules[0]["id"];
        outputAppliedRule(id);
    }else{
        findRuleInArray();
    }
}

function findRuleInArray(){ 
    applicableRules.forEach(function(i){
        if(i["evaluation"]=="Equal_to"){
            equalsRuleApplied.push(i["id"]);
        }else if(i["evaluation"] == "Greater_Than_Equal_to" && i["ruleSeverity"] == i["triggerSeverity"]){
            greaterThanEqualsRuleWithMatchingSeverity.push(i["id"]);
        }else if(i["evaluation"] == "Greater_Than"){
            greaterThanRules.push(i["id"]);
        }else if(i["evaluation"] == "Greater_Than_Equal_to" && i["ruleSeverity"] != i["triggerSeverity"]){
            greaterThanEqualsRule.push(i["id"]);
        }
    });
    getEvaluationPriority();
}

function getEvaluationPriority(){
    // If there are more than 1 rules we need a priority 
    if(equalsRuleApplied.length > 0){
        var id = equalsRuleApplied[0];
        outputAppliedRule(id);
    }else if(greaterThanEqualsRuleWithMatchingSeverity.length > 0){
        var id = greaterThanEqualsRuleWithMatchingSeverity[0];
        outputAppliedRule(id);
    }else if(greaterThanRules.length > 0){
        var id = greaterThanRules[0];
        outputAppliedRule(id);
    }else if(greaterThanEqualsRule.length > 0){
        var id = greaterThanEqualsRule[0];
        outputAppliedRule(id);
    }
}

function outputAppliedRule(ruleId){
    logger.info("Rule chosen based on severity and severityEvaluation: "+ ruleId);
    var appliedRule = executor.subject.getOutFieldSchemaHelper("rules").createNewInstance();
    appliedRule.add(ruleId);
    taskOutputEvent.put("rules", appliedRule);
}