var returnValueType = Java.type("java.lang.Boolean");
var returnValue = new returnValueType(true);

load("nashorn:mozilla_compat.js");
importClass(java.util.ArrayList);

var logger = executor.logger;
var taskOutputEvent = executor.outFields;
var taskInputEvent = executor.inFields;
var context = executor.getContextAlbum("ruleContext");
var historyContext = executor.getContextAlbum("historyContext");
var actionDateTime = new Date(Date.now()).toUTCString();
logger.info("Issuing PRIMARY Action");
var rulesFromState = taskInputEvent.get("rules");
var ruleID = rulesFromState[0];

var historyContextID = taskInputEvent.get("faultAssetId");

var rule = context.get(ruleID);
var history = historyContext.get(historyContextID);
var rulePrimaryAction = rule.get("primaryAction");
var ruleAlternativeAction = rule.get("alternativeAction");
var arrayOfTimestamps = [];
if(ruleAlternativeAction != "null"){
    if(history!=null){
        arrayOfTimestamps = JSON.parse(history.get("arrayActionTimestamps"));
    }
    arrayOfTimestamps.splice(arrayOfTimestamps.length, 0, actionDateTime);
}

var executionLogDataObject = {};
var actionAsJSON = {};
var triggerAsJSON = mapToJsonObject(taskInputEvent.entrySet());
executionLogDataObject.engineId = 45;
executionLogDataObject.instanceId = 0;
executionLogDataObject.action = String(rulePrimaryAction);
executionLogDataObject.eventType = taskInputEvent.get("eventType");
executionLogDataObject.assetType = taskInputEvent.get("faultAssetType");
executionLogDataObject.assetName = taskInputEvent.get("faultAssetName");
executionLogDataObject.status = "In Progress";

var newHistoryContextObject = executor.getContextAlbum("historyContext")
.getSchemaHelper().createNewInstance();
newHistoryContextObject.put("eventType", taskInputEvent.get("eventType"));
newHistoryContextObject.put("faultAssetType", taskInputEvent.get("faultAssetType"));
newHistoryContextObject.put("faultAssetName", taskInputEvent.get("faultAssetName"));
newHistoryContextObject.put("action", rulePrimaryAction);
newHistoryContextObject.put("actionTimestamp", actionDateTime);
newHistoryContextObject.put("arrayActionTimestamps", JSON.stringify(arrayOfTimestamps));

historyContext.put(historyContextID, newHistoryContextObject);
logger.info("\n\n\t######### Updated Action History Context: #########\n\n"
                + executor.getContextAlbum("historyContext").values()
                + "\n\t########## END OF HISTORY CONTEXT ##########\n");

taskOutputEvent.put("action", rulePrimaryAction);
actionAsJSON.action = String(rulePrimaryAction);
taskOutputEvent.put("faultAssetType", taskInputEvent.get("faultAssetType"));
actionAsJSON.faultAssetType = taskInputEvent.get("faultAssetType");
taskOutputEvent.put("faultAssetName", taskInputEvent.get("faultAssetName"));
actionAsJSON.faultAssetName = taskInputEvent.get("faultAssetName");
taskOutputEvent.put("faultAssetId", taskInputEvent.get("faultAssetId"));
actionAsJSON.faultAssetId = taskInputEvent.get("faultAssetId");
taskOutputEvent.put("vdcName", taskInputEvent.get("vdcName"));
actionAsJSON.vdcName = taskInputEvent.get("vdcName");
taskOutputEvent.put("vdcId", taskInputEvent.get("vdcId"));
actionAsJSON.vdcId = taskInputEvent.get("vdcId");
taskOutputEvent.put("actionDateTime", actionDateTime);
actionAsJSON.actionDateTime = actionDateTime;
taskOutputEvent.put("additionalText", taskInputEvent.get("additionalText"));
actionAsJSON.additionalText = taskInputEvent.get("additionalText");
actionAsJSON.tenantName = taskInputEvent.get("tenantName");
taskOutputEvent.put("tenantName", taskInputEvent.get("tenantName"));
actionAsJSON.subtenantName = taskInputEvent.get("subtenantName");
taskOutputEvent.put("subtenantName", taskInputEvent.get("subtenantName"));

triggerAsJSON.name = "Trigger";
triggerAsJSON.nameSpace = "org.onap.policy.apex.ecm";
triggerAsJSON.version = "1.0";
triggerAsJSON.source = "CENX";
triggerAsJSON.target = "APEX";

actionAsJSON.name = "Action";
actionAsJSON.nameSpace = "org.onap.policy.apex.ecm";
actionAsJSON.version = "0.0.1";
actionAsJSON.source = "APEX";
actionAsJSON.target = "ECM";

// Creating affectedObjects array, then an object and inserting it into
// the array
var genericDataRecordType = Java
        .type("org.apache.avro.generic.GenericData.Record");
var aObjects = executor.subject.getOutFieldSchemaHelper("affectedObjects")
        .createNewInstance();
var aObjectRecordSchema = executor.subject.getOutFieldSchemaHelper(
        "affectedObjects").getAvroSchema().getElementType();
var affectedObjectsJSONArray = [];
// Handle empty and handle more than 1 affectedObjects in the array
var aObjectsLength = taskInputEvent.get("affectedObjects").length;

var i;
for (i = 0; i < aObjectsLength; i++) {
    var aObjectRecord = new genericDataRecordType(aObjectRecordSchema);
    aObjectRecord.put("assetType", taskInputEvent.get("affectedObjects").get(i).get("assetType"));
    aObjectRecord.put("assetName", taskInputEvent.get("affectedObjects").get(i).get("assetName"));
    aObjectRecord.put("assetId", taskInputEvent.get("affectedObjects").get(i).get("assetId"));
    aObjectRecord.put("vdcName", taskInputEvent.get("affectedObjects").get(i).get("vdcName"));
    aObjectRecord.put("vdcId", taskInputEvent.get("affectedObjects").get(i).get("vdcId"));
    aObjects.add(aObjectRecord);
    affectedObjectsJSONArray.push(JSON.parse(taskInputEvent.get("affectedObjects").get(i)));
}
taskOutputEvent.put("affectedObjects", aObjects);
actionAsJSON.affectedObjects = affectedObjectsJSONArray;
triggerAsJSON.affectedObjects = affectedObjectsJSONArray;
triggerAsJSON.threshold = JSON.parse(taskInputEvent.get("threshold").get(0));
actionAsJsonString = JSON.stringify(actionAsJSON);
triggerAsJsonString = JSON.stringify(triggerAsJSON);
executionLogDataObject.rawMessageInput = triggerAsJsonString;
executionLogDataObject.rawMessageOutput = actionAsJsonString;

logger.info("\n\n:::::::::::::: EXECUTION LOG OUTPUT ::::::::::::::\n\n");
logger.info(JSON.stringify(executionLogDataObject));
logger.info("\n\n::::::::::::::::::::::::::::::::::::::::::::::::::\n\n");

sendRESTRequestToNBI(executionLogDataObject);