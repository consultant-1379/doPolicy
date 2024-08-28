var returnValueType = Java.type("java.lang.Boolean");
var returnValue = new returnValueType(true);

load("nashorn:mozilla_compat.js");
importClass(java.util.ArrayList);

var logger = executor.logger;
var taskOutputEvent = executor.outFields;
var taskInputEvent = executor.inFields;
var context = executor.getContextAlbum("ruleContext");
var historyContext = executor.getContextAlbum("historyContext");
logger.info("Issuing LOG ONLY Action");

var historyContextID = taskInputEvent.get("faultAssetId");
var history = historyContext.get(historyContextID);
var arrayOfTimeStamps = [];
var actionDateTime = new Date(Date.now()).toUTCString();
if(history!=null){
    arrayOfTimeStamps = history.get("arrayActionTimestamps");
}

var executionLogDataObject = {};
var actionAsJSON = {};
var triggerAsJSON = mapToJsonObject(taskInputEvent.entrySet());
executionLogDataObject.engineId = 45;
executionLogDataObject.instanceId = 0;
executionLogDataObject.action = "Log Only";
executionLogDataObject.eventType = taskInputEvent.get("eventType");
executionLogDataObject.assetType = taskInputEvent.get("faultAssetType");
executionLogDataObject.assetName = taskInputEvent.get("faultAssetName");
executionLogDataObject.status = "Completed";

var newHistoryContextObject = executor.getContextAlbum("historyContext")
.getSchemaHelper().createNewInstance();
newHistoryContextObject.put("eventType", taskInputEvent.get("eventType"));
newHistoryContextObject.put("faultAssetType", taskInputEvent.get("faultAssetType"));
newHistoryContextObject.put("faultAssetName", taskInputEvent.get("faultAssetName"));
newHistoryContextObject.put("action", "logOnly");
newHistoryContextObject.put("actionTimestamp", actionDateTime);
newHistoryContextObject.put("arrayActionTimestamps", arrayOfTimeStamps);
historyContext.put(historyContextID, newHistoryContextObject);

actionAsJSON.action = "logOnly";
actionAsJSON.faultAssetType = taskInputEvent.get("faultAssetType");
actionAsJSON.faultAssetName = taskInputEvent.get("faultAssetName");
actionAsJSON.faultAssetId = taskInputEvent.get("faultAssetId");
actionAsJSON.vdcName = taskInputEvent.get("vdcName");
actionAsJSON.vdcId = taskInputEvent.get("vdcId");
actionAsJSON.actionDateTime = actionDateTime;
actionAsJSON.additionalText = taskInputEvent.get("additionalText");
actionAsJSON.tenantName = taskInputEvent.get("tenantName");
actionAsJSON.subtenantName = taskInputEvent.get("subtenantName");

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
var genericDataRecordType = Java.type("org.apache.avro.generic.GenericData.Record");
var aObjects = executor.subject.getOutFieldSchemaHelper("affectedObjects").createNewInstance();
var aObjectRecordSchema = executor.subject.getOutFieldSchemaHelper("affectedObjects").getAvroSchema().getElementType();
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
returnValue = executor.FALSE;