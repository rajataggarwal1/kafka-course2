package com.course.kafkaexample;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class ProducerDemoWithCallBack {
    public static void main(String[] args) {
        // Creating logger using slf4j
        final Logger logger= LoggerFactory.getLogger(ProducerDemoWithCallBack.class);

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


            // Create a producer record
            ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "Hello World" + i);
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
            });
            // producer.send(record);
        }
            // Flush data
            producer.flush();
            // Flush and close producer
            producer.close();

    }

}
