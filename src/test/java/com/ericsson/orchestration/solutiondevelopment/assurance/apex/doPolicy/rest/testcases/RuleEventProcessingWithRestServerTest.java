
package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.rest.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.basicmodel.concepts.ApexException;
import org.onap.policy.apex.model.utilities.TextFileUtils;
import org.onap.policy.apex.service.engine.main.ApexMain;

import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.JSONUtils;
import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.RestClient;

public class RuleEventProcessingWithRestServerTest
{
    private static final String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
    RestClient client = new RestClient();

    @AfterClass
    public static void deleteTempFiles()
    {
        new File("src/test/resources/events/RuleOutEventForRest.json").delete();
    }

    @Test
    // Test001 - Verify that a valid PM Restart RuleOut Event is created over REST API - PrimaryAction is RestartVM"
    public void test001PostRequestForValidRestartRule() throws ApexException, IOException
    {

        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");

        String[] args =
        { "src/test/resources/config/RuleOutEventConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(200);
        apexMain.shutdown();

        final String receivedOutputString = TextFileUtils.getTextFileAsString("src/test/resources/events/RuleOutEventForRest.json").replaceAll("\\s+", "");
        final String expectedOutputString = TextFileUtils.getTextFileAsString("src/test/resources/events/PmRuleEventOutput.json").replaceAll("\\s+", "");

        // Test001 - Comparing received Rule output with expected Rule output
        assertEquals(expectedOutputString, receivedOutputString);
    }

    @Test
    // Test004 - Verify that a valid COMM RuleOut Event is created over REST API - PrimaryAction is recreatevms"
    public void test004PostRequestForValidCOMMRestartvmsRule() throws ApexException, IOException
    {

        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommSingleRuleEvent.json");

        String[] args =
        { "src/test/resources/config/RuleOutCOMMConfigurationRestEventProcessingRestServerTest004.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(200);
        apexMain.shutdown();

        final String receivedOutputString = TextFileUtils.getTextFileAsString("src/test/resources/events/RuleOutEventForRest.json").replaceAll("\\s+", "");
        final String expectedOutputString = TextFileUtils.getTextFileAsString("src/test/resources/events/CommRuleEventOutput.json").replaceAll("\\s+", "");

        // Comparing received Rule output with expected Rule output for COMM Host Unreachable
        //  Test_004 Rule out Event Format - File contents assertion
        assertEquals(expectedOutputString, receivedOutputString);
    }
    
    @Test
    // Test002 - Verify that doPolicy throws an error message on receipt of an invalid Rule (missing eventType)"
    public void test002PostRequestForInValidRule() throws ApexException, IOException
    {

        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/InvalidRuleEvent.json");
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        String[] args =
        { "src/test/resources/config/RuleOutEventConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        client.postRequest(URL, rulePayload);

        ThreadUtilities.sleep(200);
        apexMain.shutdown();

        final String outString = outContent.toString();

        System.setOut(stdout);
        System.setErr(stderr);

        // For invalid Rule event, error for mandatory field "eventtype missing" should come
        assertTrue(outString.contains("Expected field name not found: eventType"));

    }

    @Test
    // Test003 - Verify that a valid PM Recreate RuleOut Event is created over REST API - PrimaryAction is RecreateVM"
    public void test003PostRequestForValidRecreateRule() throws ApexException, IOException, ParseException
    {

        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RecreateRuleEvent.json");

        String[] args =
        { "src/test/resources/config/RuleOutEventConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(200);
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/RuleOutEventForRest.json");

        // Read Output  json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        receivedFile.close();

        // Check received Rule fields with expected Rule fields
        assertEquals(((Map<?, ?>) receivedJsonElement).get("id"), "PM-VM-CustomerABC-cpu_util-Greater_Than_Equal_to-major");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("nameSpace"), "org.onap.policy.apex.do.rule.engine");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("name"), "RuleOutEvent");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("source"), "APEX");
    }
}
