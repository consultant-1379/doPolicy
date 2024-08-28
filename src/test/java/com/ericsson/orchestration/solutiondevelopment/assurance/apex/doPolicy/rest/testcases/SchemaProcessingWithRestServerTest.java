
package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.rest.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.utilities.TextFileUtils;
import org.onap.policy.apex.service.engine.main.ApexMain;

import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.RestClient;

public class SchemaProcessingWithRestServerTest {

    @AfterClass
    public static void deleteTempFiles()
    {
        new File("src/test/resources/events/ActionOutEventForRest.json").delete();
    }

    private static final String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
    RestClient client = new RestClient();

    @Test
    public void test001RuleEmptyArray() throws Exception {
        // Test001 Verify that APEX processes a Rule event with an empty array
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/EmptyRuleEvent.json");
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        String[] args =
            { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);
        apexMain.shutdown();
        final String outString = outContent.toString();
        System.setOut(stdout);
        System.setErr(stderr);
        assertTrue(outString.contains("{rule=[]}"));
    }

    @Test
    public void test002AffectedObjectsEmptyArray() throws Exception {
        // Test002 Verify that APEX processes trigger messages with empty AffectedObjects array
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/EmptyAffectedObjectsTriggerEvent.json");
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        String[] args =
            { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);
        client.postRequest(URL, triggerPayload);
        ThreadUtilities.sleep(1000);
        apexMain.shutdown();
        final String outString = outContent.toString();
        System.setOut(stdout);
        System.setErr(stderr);
        assertTrue(outString.contains("affectedObjects"));
        assertTrue(outString.contains("[]"));
    }

    @Test
    public void test003HistoryContextUniqueIdDoesNotExist() throws Exception {
        // Test003 Verify that there is no record of the faultAssetID in the history context album
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        String[] args =
            { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);
        client.postRequest(URL, triggerPayload);
        ThreadUtilities.sleep(1000);
        apexMain.shutdown();
        final String outString = outContent.toString();
        System.setOut(stdout);
        System.setErr(stderr);
        assertTrue(outString.contains("No Action History Context Found for"));
    }

    @Test
    public void test004HistoryContextUniqueIDExists() throws Exception {
        // Test004 Verify that the faultAssetID exists in the history context album
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        String[] args =
            { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);
        client.postRequest(URL, triggerPayload);
        ThreadUtilities.sleep(1000);
        client.postRequest(URL, triggerPayload);
        ThreadUtilities.sleep(1000);
        apexMain.shutdown();
        final String outString = outContent.toString();
        System.setOut(stdout);
        System.setErr(stderr);
        assertTrue(outString.contains("Action History Context Found for"));
    }

    @Test
    public void test005HistoryContextUniqueIDMultipleEntries() throws Exception {
        // Test005 Verify that there are unique instances in the history context album for each unique faultAssetId with the same tenant
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String VM1TriggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/SchemaTest_Test_005_Trigger_Event_VM1.json");
        String VM2TriggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/SchemaTest_Test_005_Trigger_Event_VM2.json");
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        String[] args =
            { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);
        client.postRequest(URL, VM1TriggerPayload);
        ThreadUtilities.sleep(1000);
        client.postRequest(URL, VM2TriggerPayload);
        ThreadUtilities.sleep(1000);
        apexMain.shutdown();
        final String outString = outContent.toString();
        System.setOut(stdout);
        System.setErr(stderr);
        assertTrue(outString.contains("VM11111"));
        assertTrue(outString.contains("VM22222"));
    }
}