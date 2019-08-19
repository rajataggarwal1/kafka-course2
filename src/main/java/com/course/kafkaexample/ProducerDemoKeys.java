package com.course.kafkaexample;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;


public class ProducerDemoKeys {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        // Creating logger using slf4j
        final Logger logger= LoggerFactory.getLogger(ProducerDemoKeys.class);

        String  bootstrapServers="127.0.0.1:9092";
        // create producer properties
           Properties properties    = new Properties();

           // We are hardcoding the properties , we can user better approach using  ProducerConfig.
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        KafkaProducer<String,String> producer  =new KafkaProducer<String,String>(properties);

        // We can put all code in loop to produce more message

        for(int i=0; i<10; i++) {
             // Refactor code and add keys
            String topic ="first_topic";
            String value = "Hello World " + Integer.toString(i);
            String key= "id_"+ Integer.toString(i);

            // Create a producer record
            // By providing key we make sure that key/value always go to same partition.
            // Even if you run multiple times the result will be same.
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic,key, value);

            logger.info ("Key:" + key);   // log the key
              // id_0 is going to Partition:1
            // id_1 is going to Partition:0
            
            // send data -- thsis asyncronous call     now with callback
            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    // execute everytime a record is successfully sent or an exception is thrown
                    if (e == null) {
                        // Prining Topic info
                        logger.info("Received new Metada \n" + "Topic: " + recordMetadata.topic() + "\n" +
                                "Partition:" + recordMetadata.partition() + "\n" +
                                "Offset:" + recordMetadata.offset() + "\n" +
                                "TimeStamp :" + recordMetadata.timestamp());
                        // record was successfully sent

                    } else {
                        logger.error("Error while producing ", e);
                    }
                }
            }).get();   // adding get method to make call synchronous but this is bad practic  . It is making send synchronous.
            // Do not do in production
            
            // producer.send(record);
        }
            // Flush data
            producer.flush();
            // Flush and close producer
            producer.close();

    }

}
