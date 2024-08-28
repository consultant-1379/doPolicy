var returnValueType = Java.type("java.lang.Boolean");
var returnValue = new returnValueType(true);

load("nashorn:mozilla_compat.js");

var logger = executor.logger;
var taskOutputEvent = executor.outFields;
var taskInputEvent = executor.inFields;

logger.info(executor.subject.id);
logger.info("########## Received Rule event ########## \n" + taskInputEvent);

var context = executor.getContextAlbum("ruleContext");

context.forEach(function(i){
    context.remove(i);
});

var i;
for (i = 0; i < taskInputEvent.get("rule").length; i++) {

    var newRuleRequest = executor.getContextAlbum("ruleContext").getSchemaHelper().createNewInstance();

    newRuleRequest.put("eventType", taskInputEvent.get("rule").get(i).get("eventType"));
    newRuleRequest.put("faultAssetType", taskInputEvent.get("rule").get(i).get("faultAssetType"));
    newRuleRequest.put("severityEvaluation", taskInputEvent.get("rule").get(i).get("severityEvaluation"));
    newRuleRequest.put("severity", taskInputEvent.get("rule").get(i).get("severity"));
    newRuleRequest.put("tenantName", taskInputEvent.get("rule").get(i).get("tenantName"));
    newRuleRequest.put("meterName", taskInputEvent.get("rule").get(i).get("meterName"));
    newRuleRequest.put("primaryAction", taskInputEvent.get("rule").get(i).get("primaryAction"));
    newRuleRequest.put("actionCount", taskInputEvent.get("rule").get(i).get("actionCount"));
    newRuleRequest.put("timeLapse", taskInputEvent.get("rule").get(i).get("timeLapse"));
    newRuleRequest.put("alternativeAction", taskInputEvent.get("rule").get(i).get("alternativeAction"));

    if(taskInputEvent.get("rule").get(i).get("eventType") == "PM" || taskInputEvent.get("rule").get(i).get("eventType") == "FM"){
        var id = taskInputEvent.get("rule").get(i).get("eventType") + "-"
        + taskInputEvent.get("rule").get(i).get("faultAssetType") + "-"
        + taskInputEvent.get("rule").get(i).get("tenantName")+ "-"
        + taskInputEvent.get("rule").get(i).get("meterName")+ "-"
        + taskInputEvent.get("rule").get(i).get("severityEvaluation")+ "-"
        + taskInputEvent.get("rule").get(i).get("severity");
    }else if(taskInputEvent.get("rule").get(i).get("eventType") == "COMM"){
        var id = taskInputEvent.get("rule").get(i).get("eventType") + "-"
        + taskInputEvent.get("rule").get(i).get("faultAssetType");
    }
    context.put(id,newRuleRequest);
    logger.info("########## Generated ID: "+id+" ########## \n");
}
taskOutputEvent.put("id", id);