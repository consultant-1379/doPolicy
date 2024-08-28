package com.ericsson.orchestration.solutiondevelopment.assurance.apex.doPolicy.kafka.testcases;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.onap.policy.apex.core.infrastructure.threading.ThreadUtilities;
import org.onap.policy.apex.model.utilities.TextFileUtils;

public class KafkaTriggerProducer implements Runnable
{
    private final String topic;
    private final String kafkaServerAddress;
    private AtomicInteger triggerSentCount = new AtomicInteger(0);

    private Producer<String, String> producer;

    private final Thread producerThread;
    private AtomicBoolean sendTriggerFlag = new AtomicBoolean(false);
    private boolean stopFlag = false;
    private String trigger;

    public KafkaTriggerProducer(final String topic, final String kafkaServerAddress, String trigger)
    {
        this.topic = topic;
        this.kafkaServerAddress = kafkaServerAddress;
        this.trigger = trigger;

        producerThread = new Thread(this);
        producerThread.start();
    }

    @Override
    public void run()
    {
        final Properties kafkaProducerProperties = new Properties();
        kafkaProducerProperties.put("bootstrap.servers", kafkaServerAddress);
        kafkaProducerProperties.put("groupId", "doPolicy-group-id");
        kafkaProducerProperties.put("acks", "all");
        kafkaProducerProperties.put("retries", 0);
        kafkaProducerProperties.put("batch.size", 16384);
        kafkaProducerProperties.put("linger.ms", 1);
        kafkaProducerProperties.put("buffer.memory", 33554432);
        kafkaProducerProperties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducerProperties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<String, String>(kafkaProducerProperties);

        while (producerThread.isAlive() && !stopFlag)
        {
            ThreadUtilities.sleep(50);

            if (sendTriggerFlag.get())
            {

                try
                {
                    sendTriggerFromFile(trigger);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                sendTriggerFlag.set(false);
            }
        }

        producer.close(1000, TimeUnit.MILLISECONDS);
    }

    /**
     * @param triggerFile
     * @throws IOException
     *
     */
    private void sendTriggerFromFile(String triggerFile) throws IOException
    {
        String message = TextFileUtils.getTextFileAsString(triggerFile);

        producer.send(new ProducerRecord<String, String>(topic, message));
        triggerSentCount.incrementAndGet();
        producer.flush();
        System.out.println(KafkaTriggerProducer.class.getCanonicalName() + ": sent event " + message);
    }

    public void sendTrigger()
    {
        sendTriggerFlag.set(true);
    }

    public AtomicInteger getTriggerSentCount()
    {
        return triggerSentCount;
    }

    public void shutdown()
    {
        System.out.println(KafkaTriggerProducer.class.getCanonicalName() + ": stopping");

        stopFlag = true;

        while (producerThread.isAlive())
        {
            ThreadUtilities.sleep(10);
        }

        System.out.println(KafkaTriggerProducer.class.getCanonicalName() + ": stopped");
    }

}
