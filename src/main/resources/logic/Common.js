function sendRESTRequestToNBI(logJsonObject) {
    var clientType = Java.type("javax.ws.rs.client.ClientBuilder");
    var restClient = clientType.newClient();
    var jsonString = JSON.stringify(logJsonObject);
    var entityType = Java.type("javax.ws.rs.client.Entity")
    var entity = entityType.json(jsonString);
    var response = restClient.target("http://policynbi:8080/sd/v1.0/log/").request().post(entity);
    logger.info("### NBI REST RESPONSE: "+response+" ###");
}

function mapToJsonObject(inMap) {
    jsonObj = {};
    inMap.forEach(function(entry) {
        var key = entry.getKey();
        var value = entry.getValue();
        jsonObj[key] = value;
    });
    return jsonObj;
}

function initExecutionLogObject(status, message) {
    var executionLogDataObject = {};
    executionLogDataObject.engineID = 45;
    executionLogDataObject.instanceID = 0;
    executionLogDataObject.eventType = taskInputEvent.get("eventType");
    executionLogDataObject.assetType = taskInputEvent.get("faultAssetType");
    executionLogDataObject.assetName = taskInputEvent.get("faultAssetName");
    triggerAsJSON.name = taskInputEvent.get("name");
    triggerAsJSON.nameSpace = taskInputEvent.get("nameSpace");
    triggerAsJSON.version = taskInputEvent.get("version");
    triggerAsJSON.source = taskInputEvent.get("source");
    triggerAsJSON.target = taskInputEvent.get("target");
    triggerAsJSON.affectedObjects = JSON.parse(taskInputEvent.get("affectedObjects"));
    triggerAsJSON.threshold = JSON.parse(taskInputEvent.get("threshold"));
    triggerAsJsonString = JSON.stringify(triggerAsJSON);
    executionLogDataObject.rawMessageInput = triggerAsJsonString;
    executionLogDataObject.status = status;
    executionLogDataObject.notes = message;
    executionLogDataObject.action = "";
    return executionLogDataObject;
}