package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.file.testcases;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.basicmodel.concepts.ApexException;
import org.onap.policy.apex.service.engine.main.ApexMain;

public class JUnit002InvalidRuleEventTest
{

    @AfterClass
    public static void deleteTempFiles()
    {
        new File("src/test/resources/events/RuleOutEvent.json").delete();
    }

    @Test
    // Test001 - RuleOut Event is not created"
    public void test001RuleOutEvent() throws ApexException
    {
        final String[] args =
        { "src/test/resources/config/RuleOutEventInvalidConfiguration.json" };

        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        final ApexMain apexMain = new ApexMain(args);
        ThreadUtilities.sleep(500);
        apexMain.shutdown();

        final String outString = outContent.toString();

        System.setOut(stdout);
        System.setErr(stderr);
        //  Test_002 RuleOut Event is not created as mandatory rule is missing
        assertTrue(outString.contains("Expected field name not found: eventType"));

    }
    
    @Test
    // Test002 - RuleOut Event for COMM Decision Rule is not created"
    public void test002COMMRuleOutEvent() throws ApexException
    {
        final String[] args =
        { "src/test/resources/config/RuleOutCOMMRestartvmsEventInvalidConfiguration.json" };

        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        final PrintStream stdout = System.out;
        final PrintStream stderr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        final ApexMain apexMain = new ApexMain(args);
        ThreadUtilities.sleep(500);
        apexMain.shutdown();

        final String outString = outContent.toString();

        System.setOut(stdout);
        System.setErr(stderr);
        //  Test_002 RuleOut Event is not created as mandatory rule is missing
        assertTrue(outString.contains("Expected field name not found: faultAssetType"));

    }
}
