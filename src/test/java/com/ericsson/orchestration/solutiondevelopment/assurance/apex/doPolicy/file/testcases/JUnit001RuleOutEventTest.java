package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.file.testcases;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.AfterClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.basicmodel.concepts.ApexException;
import org.onap.policy.apex.model.utilities.TextFileUtils;
import org.onap.policy.apex.service.engine.main.ApexMain;

import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.JSONUtils;

public class JUnit001RuleOutEventTest
{

    @AfterClass
    public static void deleteTempFiles()
    {
        new File("src/test/resources/events/RuleOutEvent.json").delete();
    }

    @Test
    // Test001 - Verify that a valid PM Restart RuleOut Event is created - PrimaryAction is RestartVM"
    public void test001RuleOutEventForValidRestartRule() throws ApexException, IOException
    {
        final String[] args =
        { "src/test/resources/config/RuleOutEvent001RestartConfiguration.json" };

        testFileEvents(args, "src/test/resources/events/RuleOutEvent.json");
    }

    public void testFileEvents(final String[] args, final String outFilePath) throws ApexException, IOException
    {
        getApexOutput(args, outFilePath);

        final String receivedOutputString = TextFileUtils.getTextFileAsString(outFilePath).replaceAll("\\s+", "");

        final String expectedOutputString = TextFileUtils.getTextFileAsString("src/test/resources/events/PmRuleEventOutput.json").replaceAll("\\s+", "");
        //  Test_001 Rule out Event Format - File contents assertion
        assertEquals(expectedOutputString, receivedOutputString);

    }

    @Test
    // Test002 - Verify that a valid PM Recreate RuleOut Event is created - PrimaryAction is RecreateVM"
    public void test002RuleOutEventForValidRecreateRule() throws ApexException, IOException, ParseException
    {

        final String[] args =
        { "src/test/resources/config/RuleOutEvent002RecreateConfiguration.json" };
        String outFilePath = "src/test/resources/events/RuleOutEvent.json";

        getApexOutput(args, outFilePath);

        JSONParser parser = new JSONParser();
        FileReader receivedFile = new FileReader(outFilePath);

        // Read Output  json and convert to Map
        Object receivedJsonElement = JSONUtils.convertToJsonElement(parser.parse(receivedFile));

        receivedFile.close();

        //  Test_002 Comparing Rule out Event received fields with expected action output fields
        assertEquals(((Map<?, ?>) receivedJsonElement).get("id"), "PM-VM-CustomerABC-cpu_util-Greater_Than_Equal_to-major");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("nameSpace"), "org.onap.policy.apex.do.rule.engine");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("name"), "RuleOutEvent");
        assertEquals(((Map<?, ?>) receivedJsonElement).get("source"), "APEX");

    }

    @Test
    // Test003 - Verify that a valid COMM RuleOut Event is created - PrimaryAction is recreatevms"
    public void test003RuleOutEventForValidCOMMRecreateRule() throws ApexException, IOException
    {
        final String[] args =
        { "src/test/resources/config/RuleOutEventCOMMRecreateConfiguration.json" };

        testCOMMRuleEvents(args, "src/test/resources/events/RuleOutEvent.json");
    }

    public void testCOMMRuleEvents(final String[] args, final String outFilePath) throws ApexException, IOException
    {
        getApexOutput(args, outFilePath);

        final String receivedOutputString = TextFileUtils.getTextFileAsString(outFilePath).replaceAll("\\s+", "");

        final String expectedOutputString = TextFileUtils.getTextFileAsString("src/test/resources/events/CommRuleEventOutput.json").replaceAll("\\s+", "");
        //  Test_003 Rule out Event Format - File contents assertion
        assertEquals(expectedOutputString, receivedOutputString);

    }

    private void getApexOutput(final String[] args, final String outFilePath) throws ApexException
    {
        final ApexMain apexMain = new ApexMain(args);
        final File outFile = new File(outFilePath);
        while (!outFile.exists() || outFile.length() == 0)
        {
            ThreadUtilities.sleep(1000);
        }
        apexMain.shutdown();
    }
}
