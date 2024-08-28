
package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.rest.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;

import javax.ws.rs.core.Response;

import org.json.simple.parser.JSONParser;
import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.basicmodel.concepts.ApexException;
import org.onap.policy.apex.model.utilities.TextFileUtils;
import org.onap.policy.apex.service.engine.main.ApexMain;

import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.JSONUtils;
import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.RestClient;

public class UC3WithRestServerTest
{

    private static final String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
    RestClient client = new RestClient();

    @AfterClass
    public static void deleteTempFiles()
    {
        new File("src/test/resources/events/ActionOutEventForRest.json").delete();
        new File("src/test/resources/events/UC3ActionOutEventForRest.json").delete();
    }

    @Test
    public void testUC3PrimaryActionWithRestClient() throws Exception
    {
        // Test001 Verify that APEX outputs a restart action.
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(2300);

        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader("src/test/resources/events/ActionOutEventForRest.json");
        FileReader expectedFile = new FileReader("src/test/resources/events/ExpectedRestartOutput.json");

        // Read Output and expected json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));
        Object expectedJsonElement = JSONUtils.convertToJsonElement(parser.parse(expectedFile));

        // Need to filter field "actionDateTime" as it is always generated during
        // runtime hence cannot compare with stored files
        JSONUtils.filterData("actionDateTime", receivedJsonElement);
        JSONUtils.filterData("actionDateTime", expectedJsonElement);

        receivedFile.close();
        expectedFile.close();

        // Comparing received action output with expected action output
        assertEquals(receivedJsonElement, expectedJsonElement);
        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "restart");
    }

    @Test
    public void testUC3AlternativeActionWithRestClient() throws Exception
    {
        // Test002 Verify that the alternative action is taken when the 'Action Count Greater Than' value
        // is less than the number of actions taken within the time lapse.
        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");

        String[] args =
        { "src/test/resources/config/AlternativeActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);
        new FileWriter("src/test/resources/events/UC3ActionOutEventForRest.json").close();
        response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);
        String receivedEvent = "";
        File file = new File("src/test/resources/events/UC3ActionOutEventForRest.json");
        Scanner s = new Scanner(file);
        int line = 1;
        while (s.hasNextLine())
        {
            if (line == 1)
            {
                receivedEvent += s.nextLine().trim();
            }
            else
            {
                receivedEvent += s.nextLine();
            }
            line += 1;
        }
        apexMain.shutdown();

        JSONParser parser = new JSONParser();
        FileReader expectedFile = new FileReader("src/test/resources/events/UC3Test_002_ExpectedRecreateOutput.json");

        // Read Output and expected json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedEvent));
        Object expectedJsonElement = JSONUtils.convertToJsonElement(parser.parse(expectedFile));

        // Need to filter field "actionDateTime" as it is always generated during
        // runtime hence cannot compare with stored files
        JSONUtils.filterData("actionDateTime", receivedJsonElement);
        JSONUtils.filterData("actionDateTime", expectedJsonElement);
        s.close();
        expectedFile.close();

        // Comparing received action output with expected action output
        assertEquals(receivedJsonElement, expectedJsonElement);
        assertEquals(((Map<?, ?>) receivedJsonElement).get("action"), "recreate");
    }

    @Test
    public void testUC3LogOnlyAction() throws ApexException, IOException
    {
        // Test003 Verify that a "Log Only" action when APEX receives a second trigger before the Subsequent Action Wait Time has passed.
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
        ThreadUtilities.sleep(2300);
        client.postRequest(URL, triggerPayload);
        ThreadUtilities.sleep(2300);
        client.postRequest(URL, triggerPayload);
        ThreadUtilities.sleep(2300);
        apexMain.shutdown();

        final String outString = outContent.toString();

        System.setOut(stdout);
        System.setErr(stderr);

        assertTrue(outString.contains("Issuing LOG ONLY Action"));

    }
}
