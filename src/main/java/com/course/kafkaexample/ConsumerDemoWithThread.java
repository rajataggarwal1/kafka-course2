package com.course.kafkaexample;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThread {
    public static void main(String[] args) {

        new ConsumerDemoWithThread().run();
    }

    private ConsumerDemoWithThread(){
        // Default constructor
    }
    private void run()
    {
        final Logger logger= LoggerFactory.getLogger(ConsumerDemoWithThread.class.getName());
        String  bootstrapServers="127.0.0.1:9092";
        String topic="first_topic";
        String groupID="My-sixth-Application";

        // Latch for dealing multiple thread
        CountDownLatch latch= new CountDownLatch(1);

        // Create consumer runnable
        logger.info("Creating consumer");
        Runnable myConsumerRunnable=new ConsumerRunnable(bootstrapServers,groupID,topic,latch);

        // start the thread
        Thread myThread = new  Thread(myConsumerRunnable);
        myThread.start();
         // Add shutdown hook
         Runtime.getRuntime().addShutdownHook(new Thread(() -> {
             logger.info("Caught shutdown hook");
             ((ConsumerRunnable) myConsumerRunnable).shutdown();
             try {
                 latch.await();
             } catch (InterruptedException e) {
                 logger.info("Application is exited");
             }
         }));

        
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted",e);
           
        }   finally
        {
            logger.info("Application is closing");
        }
    }

    public class ConsumerRunnable implements Runnable
    {
        private CountDownLatch latch;
        private Logger logger= LoggerFactory.getLogger(ConsumerRunnable.class.getName());
        
        private KafkaConsumer<String,String> consumer;
        Properties properties    = new Properties();

        public ConsumerRunnable(String bootStrapServer, String groupId,String topic, CountDownLatch latch)
        {
            this.latch=latch;
                    // using  ConsumerConfig.
                    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            // Create consumer
            consumer=new KafkaConsumer<String, String>(properties);
            // Subscribe consumer to topic
            consumer.subscribe(Arrays.asList(topic));
        }
        @Override
        public void run() {
            try {


                // Moved while code to thread's run method
                // Pull new Data
                while (true) {
                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100)); // This is new in Kafka 2.0.0 and also we have to enable 1.8 java language support
                    // which will add Lamda support and add entry in pom.xml
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key:" + record.key() + " ,Value:" + record.value());
                        logger.info("Received new Metada \n" +
                                "Partition:" + record.partition() + "\n" +
                                "Offset:" + record.offset() + "\n" +
                                "TimeStamp :" + record.timestamp());
                    }

                }
            }catch(WakeupException e)
            {
                logger.info ("received shutdown signal");
            }  finally
            {
                consumer.close();
                // tell main code of which we are done with main method
                latch.countDown();
            }
        }

        public void shutdown()
        {
            // Wackup method is teh special method to interrupt consumer.poll
            // it will throe exception called wackup exception
            consumer.wakeup();
        }
    }

}
