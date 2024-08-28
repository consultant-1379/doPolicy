
package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.rest.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.utilities.TextFileUtils;
import org.onap.policy.apex.service.engine.main.ApexMain;

import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.JSONUtils;
import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.RestClient;

public class ActionProcessingWithRestServerTest
{

    private static final String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
    RestClient client = new RestClient();

    @AfterClass
    public static void deleteTempFiles()
    {
        new File("src/test/resources/events/ActionOutEventForRest.json").delete();
    }
    
    @Test
    // Test001 - Verify that a restart action event is output when a valid PM restart rule and CENX trigger are matched"
    public void test001PostRequestForRestartActionWithValidTriggerAndRule() throws Exception
    {
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(1300);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");
        FileReader expectedFile = new FileReader("src/test/resources/events/ExpectedRestartOutput.json");

        // Read Output and expected json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        Object expectedJsonElement = JSONUtils.convertToJsonElement(parser.parse(expectedFile));

        // Need to filter field "actionDateTime" as it is always generated during runtime hence cannot compare with stored files
        JSONUtils.filterData("actionDateTime", receivedJsonElement);
        JSONUtils.filterData("actionDateTime", expectedJsonElement);

        receivedFile.close();
        expectedFile.close();

        // Comparing received action output with expected action output
        assertEquals(receivedJsonElement, expectedJsonElement);
        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "restart");
    }

    @Test
    // Test002 - Verify that a recreate action event is output when a valid PM restart rule and CENX trigger are matched"
    public void test002PostRequestForRecreateActionWithValidTriggerAndRule() throws Exception
    {
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(1300);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");

        // Read Output json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        receivedFile.close();

        // Comparing received action output fields with expected action output fields
        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "recreate");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetType"), "VM");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetName"), "VM-1682");
    }

    @Test
    //Test003 - Verify that a valid PM recreate VM Action Event is output when a valid CENX Trigger matches one of the multiple valid PM Rules that exists in the doPolicy"
    public void testPostRequestForRecreateActionWithValidTriggerAndMultipleRules() throws Exception
    {
        String recreateRulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RecreateRuleEventDiffTenant.json");
        String restartRulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        // First post Recreate Rule
        Response response = client.postRequest(URL, recreateRulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        // Second post Restart Rule
        response = client.postRequest(URL, restartRulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(1300);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");

        // Read Output  json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        receivedFile.close();

        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "recreate");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetType"), "VM");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetName"), "VM-1682");
    }

    @Test
    //Test004 - Verify that a valid PM restart VM Action Event is output when a valid CENX Trigger matches one of the multiple valid PM Rules that exists in the doPolicy"
    public void testPostRequestForRestartActionWithValidTriggerAndMultipleRules() throws Exception
    {
        String rulesPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RecreateRuleEventDiffTenant.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        // First post Restart Rule
        Response response = client.postRequest(URL, rulesPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(1300);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");

        // Read Output  json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        receivedFile.close();

        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "restart");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetType"), "VM");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetName"), "VM-1682");
    }

    @Test
    // Test005 - Verify that a valid COMM ActionOut Event is created when a valid Rule and valid Trigger are matched up - PrimaryAction is recreatevms"
    public void testPostRequestForCOMMRestartvmsWithValidTriggerAndRule() throws Exception
    {
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommSingleRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(1300);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");
        FileReader expectedFile = new FileReader("src/test/resources/events/CommActionOutEvent.json");

        // Read Output and expected json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        Object expectedJsonElement = JSONUtils.convertToJsonElement(parser.parse(expectedFile));

        // Need to filter field "actionDateTime" as it is always generated during runtime hence cannot compare with stored files
        JSONUtils.filterData("actionDateTime", receivedJsonElement);
        JSONUtils.filterData("actionDateTime", expectedJsonElement);

        receivedFile.close();
        expectedFile.close();

        // Test005 - Comparing received action output with expected action output
        assertEquals(receivedJsonElement, expectedJsonElement);
        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "recreatevms");
    }

    @Test
    // Test006 - Verify that a valid COMM ActionOut Event is created when multiple valid PM & COMM Rules exist and a valid Trigger matches the COMM rule - PrimaryAction is recreatevms"
    public void test006PostRequestForCOMMRecreatevmsActionWithValidTriggerAndMultipleRules() throws Exception
    {
        String recreatevmsRulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommMultipleRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, recreatevmsRulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(1300);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");

        // Read Output json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        receivedFile.close();

        // Test006 - Comparing received action output with expected action output
        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "recreatevms");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetType"), "Host");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetName"), "Host-34525");
    }

    @Test
    // Test007 - Verify that apex can handle multiple affected objects when a CENX trigger for multiple affected objects is processed and finds a matching Rule
    public void test007PostRequestForRuleAndTriggerWithMultipleVMs() throws Exception
    {
        String recreatevmsRulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommSingleRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommTriggerEventKafka.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        // First post COMM recreatevms Rule
        Response response = client.postRequest(URL, recreatevmsRulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(1300);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");

        // Read Output json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        receivedFile.close();

        // Test007 - Comparing received action output with expected action output
        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "recreatevms");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetType"), "Host");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("faultAssetName"), "Host-ActionProcessingWithRestServerTest010");

        // Test007 - Verify that multiple affected objects with different asset ids are present in action output json
        HashSet<?> affectedObjects = (HashSet<?>) ((Map<?, ?>) receivedJsonElement).get("affectedObjects");
        assertEquals((affectedObjects.size()), 3);
        List<String> assetIds = new ArrayList<String>();
        for (Object affectedObject : affectedObjects)
        {
            assetIds.add((String) ((Map<?, ?>) affectedObject).get("assetId"));
        }

        assertTrue(assetIds.contains("3234-a453235-34215"));
        assertTrue(assetIds.contains("3234-a453235-34216"));
        assertTrue(assetIds.contains("3234-a453235-34217"));
    }
}
