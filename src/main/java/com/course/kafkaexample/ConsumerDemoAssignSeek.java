package com.course.kafkaexample;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoAssignSeek {
    public static void main(String[] args) {
        // Creating logger using slf4j
        final Logger logger= LoggerFactory.getLogger(ConsumerDemoAssignSeek.class.getName());

        String  bootstrapServers="127.0.0.1:9092";
        Properties properties    = new Properties();
        String topic="first_topic";

        // using  ConsumerConfig.
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

         // Create consumer
        KafkaConsumer<String,String> consumer=new KafkaConsumer<String, String>(properties);

        //  Assign and seek mostly used to replay data or fetch a specific message
        TopicPartition partitionToReadFrom=new TopicPartition(topic,0);
        long offsetToreadFrom=15L;
        consumer.assign(Arrays.asList(partitionToReadFrom));
        
        //Seek
        consumer.seek(partitionToReadFrom,offsetToreadFrom);

        int noOfMessageToRead=5;
        boolean keepOnReading=true;
        int noOfMessageReadSoFar=0;


        // Pull new Data
        while(keepOnReading)
        {
           ConsumerRecords<String, String> records= consumer.poll(Duration.ofMillis(100)); // This is new in Kafka 2.0.0 and also we have to enable 1.8 java language support
            // which will add Lamda support and add entry in pom.xml
            for(ConsumerRecord<String,String> record:records)
            {
                noOfMessageReadSoFar+=1;

                logger.info("Key:" + record.key() + " ,Value:"+record.value());
                logger.info("Partition:" + record.partition() + "\n" +
                        "Offset:" + record.offset() + "\n" +
                        "TimeStamp :" + record.timestamp());
                if(noOfMessageReadSoFar >=noOfMessageToRead)
                {
                    keepOnReading=false;       // to exit while loop
                    break; // to exit for loop
                }

            }

        }
        logger.info("Exiting the application");

    }
}
