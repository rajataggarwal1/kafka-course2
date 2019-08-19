package com.course.kafkaexample;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoGroups {
    public static void main(String[] args) {
        // Creating logger using slf4j
        final Logger logger= LoggerFactory.getLogger(ConsumerDemoGroups.class.getName());

        String  bootstrapServers="127.0.0.1:9092";
        Properties properties    = new Properties();
        String topic="first_topic";
        String groupID="My-fifth-Application";
        // using  ConsumerConfig.
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

         // Create consumer
        KafkaConsumer<String,String> consumer=new KafkaConsumer<String, String>(properties);
        // Subscribe consumer to topic
          consumer.subscribe(Arrays.asList(topic));
         // We can subscribe to multiple topics by proving comma seperated value in subscribe method
        // ex consumer.subscribe(Arrays.asList("first_topic", "second_topic"));

        // Pull new Data
        while(true)
        {
           ConsumerRecords<String, String> records= consumer.poll(Duration.ofMillis(100)); // This is new in Kafka 2.0.0 and also we have to enable 1.8 java language support
            // which will add Lamda support and add entry in pom.xml
            for(ConsumerRecord<String,String> record:records)
            {
                logger.info("Key:" + record.key() + " ,Value:"+record.value());
                logger.info("Received new Metada \n" +
                        "Partition:" + record.partition() + "\n" +
                        "Offset:" + record.offset() + "\n" +
                        "TimeStamp :" + record.timestamp());
            }

        }

    }
}
