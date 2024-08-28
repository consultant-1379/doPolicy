/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2016-2018 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.kafka.testcases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

import javax.ws.rs.core.Response;

import org.I0Itec.zkclient.ZkClient;
import org.apache.kafka.common.utils.Time;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.apex.core.infrastructure.messaging.MessagingException;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.basicmodel.concepts.ApexException;
import org.onap.policy.apex.model.utilities.TextFileUtils;
import org.onap.policy.apex.service.engine.main.ApexMain;

import com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.utilities.RestClient;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import kafka.zk.EmbeddedZookeeper;

public class ActionProcessingWithKafkaTest
{
    // The method of starting an embedded Kafka server used in this example is based on the method
    // on slashdot at
    // https://github.com/asmaier/mini-kafka

    private static final String ZKHOST = "127.0.0.1";
    private static final String BROKERHOST = "127.0.0.1";
    private static final String BROKERPORT = "39902";

    private static EmbeddedZookeeper zkServer;
    private static ZkClient zkClient;
    private static KafkaServer kafkaServer;

    @BeforeClass
    public static void setupDummyKafkaServer() throws IOException
    {
        // setup Zookeeper
        zkServer = new EmbeddedZookeeper();
        final String zkConnect = ZKHOST + ":" + zkServer.port();
        zkClient = new ZkClient(zkConnect, 30000, 30000, ZKStringSerializer$.MODULE$);
        final ZkUtils zkUtils = ZkUtils.apply(zkClient, false);

        // setup Broker
        final Properties brokerProps = new Properties();
        brokerProps.setProperty("zookeeper.connect", zkConnect);
        brokerProps.setProperty("broker.id", "0");
        brokerProps.setProperty("log.dirs", Files.createTempDirectory("kafka-").toAbsolutePath().toString());
        brokerProps.setProperty("listeners", "PLAINTEXT://" + BROKERHOST + ":" + BROKERPORT);
        brokerProps.setProperty("offsets.topic.replication.factor", "1");
        final KafkaConfig config = new KafkaConfig(brokerProps);
        final Time mock = new MockTime();
        kafkaServer = TestUtils.createServer(config, mock);

        // create topics
        AdminUtils.createTopic(zkUtils, "Trigger", 1, 1, new Properties(), RackAwareMode.Disabled$.MODULE$);
        AdminUtils.createTopic(zkUtils, "Action", 1, 1, new Properties(), RackAwareMode.Disabled$.MODULE$);
    }

    @AfterClass
    public static void shutdownDummyKafkaServer()
    {
        if (kafkaServer != null)
        {
            kafkaServer.shutdown();
        }
        if (zkClient != null)
        {
            zkClient.close();
        }
    }

    @Test
    // Test001 - Testcase to verify Restart action with valid trigger when sent from kafka client and Rule sent as REST request
    public void test001RestartActionWithValidTriggerAndRule() throws MessagingException, ApexException, IOException
    {
        final String[] args =
        { "src/test/resources/config/ActionConfigurationForKafka.json" };
        final KafkaActionSubscriber subscriber = new KafkaActionSubscriber("Action", "localhost:" + BROKERPORT);
        final KafkaTriggerProducer producer = new KafkaTriggerProducer("Trigger", "localhost:" + BROKERPORT, "src/test/resources/events/PmTriggerEvent.json");

        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RestartRecreateRuleEvent.json");
        String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
        RestClient client = new RestClient();

        final ApexMain apexMain = new ApexMain(args);
        ThreadUtilities.sleep(1000);
        // Post rule with restart action using rest
        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);

        // Send trigger alarm using Kafka until valid action is received
        do
        {
            producer.sendTrigger();
            ThreadUtilities.sleep(1000);
        }
        while (subscriber.getActionReceivedCount().get() < 1);

        System.out.println("sent Trigger count: " + producer.getTriggerSentCount());
        System.out.println("received Action count: " + subscriber.getActionReceivedCount());

        apexMain.shutdown();
        // Verify that a valid action is received by Kafka Subscriber
        assertTrue(subscriber.getReceivedAction().size() >= 1);

        // Verify that  Restart action is received by Kafka Subscriber
        for (String receivedAction : subscriber.getReceivedAction())
        {
            assertTrue(receivedAction.contains("\"action\"" + ":" + " \"restart\""));
        }

        subscriber.shutdown();
        producer.shutdown();
    }

    @Test
    // Test002 - Testcase to verify Recreate action with valid trigger when sent from kafka client and Rule sent as REST request
    public void test002RecreateActionWithValidTriggerAndRule() throws MessagingException, ApexException, IOException
    {
        final String[] args =
        { "src/test/resources/config/ActionConfigurationForKafka.json" };
        final KafkaActionSubscriber subscriber = new KafkaActionSubscriber("Action", "localhost:" + BROKERPORT);
        final KafkaTriggerProducer producer = new KafkaTriggerProducer("Trigger", "localhost:" + BROKERPORT, "src/test/resources/events/PmTriggerEvent.json");

        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/RecreateRuleEvent.json");
        String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
        RestClient client = new RestClient();

        final ApexMain apexMain = new ApexMain(args);

        // Post rule with Recreate action using rest
        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);

        // Send trigger alarm using Kafka until valid action is received

        do
        {
            producer.sendTrigger();
            ThreadUtilities.sleep(1000);
        }
        while (subscriber.getActionReceivedCount().get() < 1);

        System.out.println("sent Trigger count: " + producer.getTriggerSentCount());
        System.out.println("received Action count: " + subscriber.getActionReceivedCount());

        apexMain.shutdown();
        // Verify that a valid action is received by Kafka Subscriber
        assertTrue(subscriber.getReceivedAction().size() >= 1);

        // Verify that  Recreate action is received by Kafka Subscriber
        for (String receivedAction : subscriber.getReceivedAction())
        {
            assertTrue(receivedAction.contains("\"action\"" + ":" + " \"recreate\""));
        }

        subscriber.shutdown();
        producer.shutdown();
    }

    @Test
    // Test003 - Verify that apex can handle multiple affected objects when a CENX trigger for multiple affected objects is processed and finds a matching Rule
    public void test003PostRequestForRuleAndTriggerWithMultipleVMs() throws MessagingException, ApexException, IOException
    {
        final String[] args =
        { "src/test/resources/config/ActionConfigurationForKafka.json" };
        final KafkaActionSubscriber subscriber = new KafkaActionSubscriber("Action", "localhost:" + BROKERPORT);
        final KafkaTriggerProducer producer = new KafkaTriggerProducer("Trigger", "localhost:" + BROKERPORT, "src/test/resources/events/CommTriggerEventKafka.json");

        String rulePayload = TextFileUtils.getTextFileAsString("src/test/resources/events/CommSingleRuleEvent.json");
        String URL = "http://localhost:12346/apex/FirstConsumer/EventIn";
        RestClient client = new RestClient();

        final ApexMain apexMain = new ApexMain(args);

        // Post rule with Recreate action using rest
        Response response = client.postRequest(URL, rulePayload);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ThreadUtilities.sleep(1000);

        // Send trigger alarm using Kafka until valid action is received

        do
        {
            producer.sendTrigger();
            ThreadUtilities.sleep(1000);
        }
        while (subscriber.getActionReceivedCount().get() < 1);

        System.out.println("sent Trigger count: " + producer.getTriggerSentCount());
        System.out.println("received Action count: " + subscriber.getActionReceivedCount());

        apexMain.shutdown();
        // Verify that a valid action is received by Kafka Subscriber
        assertTrue(subscriber.getReceivedAction().size() >= 1);

        // Verify that  Recreate action is received by Kafka Subscriber

        String receivedAction = subscriber.getReceivedAction().get(0);
        assertTrue(receivedAction.contains("\"action\"" + ":" + " \"recreatevms\""));
        assertTrue(receivedAction.contains("3234-a453235-34215"));
        assertTrue(receivedAction.contains("3234-a453235-34216"));
        assertTrue(receivedAction.contains("3234-a453235-34217"));

        subscriber.shutdown();
        producer.shutdown();
    }

}
