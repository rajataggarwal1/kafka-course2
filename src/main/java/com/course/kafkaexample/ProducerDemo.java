package com.course.kafkaexample;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerDemo {
    public static void main(String[] args) {

        String  bootstrapServers="127.0.0.1:9092";
        // create producer properties
           Properties properties    = new Properties();

           // We are hardcoding the properties , we can user better approach using  ProducerConfig.
          /*  properties.setProperty("bootstrap.servers", bootstrapServers);
        //StringSerializer is part of kafka library. when we import kafka then it come with it
            properties.setProperty("key.serializer", StringSerializer.class.getName());
            properties.setProperty("value.serializer", StringSerializer.class.getName());    */
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Create the producer
        KafkaProducer<String,String> producer  =new KafkaProducer<String,String>(properties);

        // Create a producer record
        ProducerRecord<String,String> record= new ProducerRecord<String, String>("first_topic", "Hello World");
        // send data -- thsis asyncronous call
         producer.send(record);
        // Flush data
         producer.flush();
         // Flush and close producer
        producer.close();
    }

}
