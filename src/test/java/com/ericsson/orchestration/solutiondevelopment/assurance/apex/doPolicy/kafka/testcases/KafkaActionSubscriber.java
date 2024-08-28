package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.kafka.testcases;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.onap.policy.apex.core.infrastructure.messaging.MessagingException;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;

public class KafkaActionSubscriber implements Runnable
{
    private final String topic;
    private final String kafkaServerAddress;
    private AtomicInteger actionReceivedCount = new AtomicInteger(0);
    private List<String> receivedActions = new ArrayList<>();

    KafkaConsumer<String, String> consumer;

    Thread subscriberThread;

    public KafkaActionSubscriber(final String topic, final String kafkaServerAddress) throws MessagingException
    {
        this.topic = topic;
        this.kafkaServerAddress = kafkaServerAddress;

        final Properties props = new Properties();
        props.put("bootstrap.servers", kafkaServerAddress);
        props.put("group.id", "doPolicy-group-id");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topic));

        subscriberThread = new Thread(this);
        subscriberThread.start();
    }

    @Override
    public void run()
    {
        System.out.println(KafkaActionSubscriber.class.getCanonicalName() + ": receiving events from Kafka server at "
                + kafkaServerAddress + " on topic " + topic);

        while (subscriberThread.isAlive() && !subscriberThread.isInterrupted())
        {
            try
            {
                final ConsumerRecords<String, String> records = consumer.poll(100);
                for (final ConsumerRecord<String, String> record : records)
                {
                    actionReceivedCount.incrementAndGet();
                    System.out.println("******");
                    System.out.println("offset=" + record.offset());
                    System.out.println("key=" + record.key());
                    System.out.println("name=" + record.value());
                    receivedActions.add(record.value());
                }
            }
            catch (final Exception e)
            {
                break;
            }
        }

        System.out.println(KafkaActionSubscriber.class.getCanonicalName() + ": action reception completed");
    }

    public AtomicInteger getActionReceivedCount()
    {
        return actionReceivedCount;
    }

    public List<String> getReceivedAction()
    {
        return receivedActions;
    }

    public void shutdown()
    {
        subscriberThread.interrupt();

        while (subscriberThread.isAlive())
        {
            ThreadUtilities.sleep(10);
        }

        consumer.close();
        System.out.println(KafkaActionSubscriber.class.getCanonicalName() + ": stopped");
    }

}
