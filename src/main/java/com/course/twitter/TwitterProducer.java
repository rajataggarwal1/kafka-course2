package com.course.twitter;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

// Created class to behave as client and will get tweet related to bitcoin.
public class TwitterProducer {

    public TwitterProducer(){}
    
   final Logger logger= LoggerFactory.getLogger(TwitterProducer.class.getName());

    private String consumerKey ="80ShBbdn4KUUqYhDcIXVvQ6At";
    private String consumerSecret="g8efh85ddMGOWlC2UWFMqrl8GTvpNXbH8P93Ktl1skoVtG0Ria";
    private String token="3158079492-TLOhW1diXl0lp4BlUwDVkac675mdfsJ0Yl0tB5Q";
    private String secret="w5zsZDdjdx6wCjY7Ew8uLCcx2p6LWPTbMoQ0S6QHo0yGt";
    // now chaing to follow kafka after adding producer   
     List<String> terms = Lists.newArrayList("kafka");    

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

     // Created method to have logic of conecting twitter and getting tweet
    public void run()  {
         logger.info("Setup");
       // connecting twitter client
        /** Set up your blocking queues: Be sure to size these properly based on expected TPS of your stream */
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(1000);
        Client hosebirdClient=buildTwitterClient(msgQueue);
        // Attempts to establish a connection.
        hosebirdClient.connect();

        // creating twitter Producer
        KafkaProducer<String,String> producer=createKafkaProducer();

        // Adding shutdown hook.

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Stopping Application");
            logger.info("Shutting down client from twitter");
            hosebirdClient.stop();
            logger.info("Closing producerr");
            producer.close();
            logger.info("Done !");
        }));


        while (!hosebirdClient.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                hosebirdClient.stop();
            }
            if(msg != null)
            {
                  logger.info(msg);
                  // Consuming message using producer
                  producer.send(new ProducerRecord<String, String>("twitter_tweet", null, msg), new Callback() {
                      @Override
                      public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                       // execute everytime a record is successfully sent or an exception is thrown
                       if (e != null) {
                           // Prining Topic info
                           logger.error("Error ocured" + e.getMessage());
                           // record was successfully sent

                       }

                      }
                  });
            }

        }
         logger.info("End of Application");


    }

     public Client buildTwitterClient(BlockingQueue msgQueue)
     {
         //  Connecting with host providing secert key provided by twitter api
         /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
         Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
         StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
         // Optional: set up some followings and track terms
         // below is for following people. currently we are going to follow terms so commenting below
         //List<Long> followings = Lists.newArrayList(1234L, 566788L);
        // hosebirdEndpoint.followings(followings);      // commenting following endpoint as well
         //List<String> terms = Lists.newArrayList("bitcoin");
         // Moving to yop


         hosebirdEndpoint.trackTerms(terms);

         // These secrets should be read from a config file
         Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

         ClientBuilder builder = new ClientBuilder()
                 .name("Hosebird-Client-01")                              // optional: mainly for the logs
                 .hosts(hosebirdHosts)
                 .authentication(hosebirdAuth)
                 .endpoint(hosebirdEndpoint)
                 .processor(new StringDelimitedProcessor(msgQueue));

         // We donot need at this time.
                // .eventMessageQueue(eventQueue);                          // optional: use this if you want to process client events

         Client hosebirdClient = builder.build();

         return hosebirdClient;


     }
    // Creating Kafka producer which will produce the message
     public KafkaProducer<String, String> createKafkaProducer()
     {
         String  bootstrapServers="127.0.0.1:9092";
        // create producer properties
        Properties properties    = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        // Create the producer
        KafkaProducer<String,String> producer  =new KafkaProducer<String,String>(properties);

        return   producer;
                                                                                                            

     }

}
