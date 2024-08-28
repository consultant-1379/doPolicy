
package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.rest.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.basicmodel.concepts.ApexException;
import org.onap.policy.apex.model.utilities.TextFileUtils;
import org.onap.policy.apex.service.engine.main.ApexMain;

import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.RestClient;

public class TriggerEventProcessingWithRestServerTest
{
    private static final String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
    RestClient client = new RestClient();

    @AfterClass
    public static void deleteTempFiles()
    {
        new File("src/test/resources/events/ActionOutEventForRest.json").delete();
    }

    @Test
    // Test001 - Verify that apex doPolicy handles a valid CENX trigger sent over REST API"
    public void test001PostRequestForValidTriggerEvent() throws ApexException, IOException
    {
    	final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/PmTriggerEvent.json");
        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        Response response = client.postRequest(URL, triggerPayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreadUtilities.sleep(400);
        apexMain.shutdown();

        final String outString = outContent.toString();

        System.setOut(stdout);
        System.setErr(stderr);

        // For invalid trigger event, error for mandatory field "eventType missing" should come
        assertTrue(outString.contains("No Rule Exists"));
    }

    @Test
    // Test002 - Verify that apex doPolicy logs on receipt of an invalid CENX trigger sent over REST API"
    public void test002PostRequestForInValidTriggerEvent() throws ApexException, IOException
    {

        String triggerPayload = TextFileUtils.getTextFileAsString("src/test/resources/events/InvalidTriggerEvent.json");
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        String[] args =
        { "src/test/resources/config/ActionConfigurationForRest.json" };
        ApexMain apexMain = new ApexMain(args);

        client.postRequest(URL, triggerPayload);

        ThreadUtilities.sleep(400);
        apexMain.shutdown();

        final String outString = outContent.toString();

        System.setOut(stdout);
        System.setErr(stderr);

        // For invalid trigger event, error for mandatory field "eventType missing" should come
        assertTrue(outString.contains("Field \"eventType\" is missing, but is mandatory"));

    }
}
