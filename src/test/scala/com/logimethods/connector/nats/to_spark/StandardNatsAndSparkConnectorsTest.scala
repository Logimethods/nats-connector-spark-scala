package com.logimethods.connector.nats.to_spark

import com.logimethods.scala.connector.spark.to_nats._

import org.apache.spark.storage.StorageLevel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;

import com.logimethods.connector.nats.spark.test.UnitTestUtilities;
import com.logimethods.connector.nats.spark.test.UnitTestUtilities._;

import com.logimethods.connector.nats.spark.test.NatsPublisher;
import com.logimethods.connector.nats.spark.test.NatsSubscriber;

import com.logimethods.connector.nats.spark.test.StandardNatsPublisher;
import com.logimethods.connector.nats.spark.test.StandardNatsSubscriber;

class StandardNatsAndSparkConnectorsTest extends AbstractNatsAndSparkConnectorsTest { 
  	
  test("NatsSubscriber should receive NATS messages DIRECTLY from NatsPublisher") {
		val executor = Executors.newFixedThreadPool(12);

		val nbOfMessages = 5;
		val np = getNatsPublisher(nbOfMessages);

		val ns: NatsSubscriber = getNatsSubscriber(DEFAULT_SUBJECT, nbOfMessages)
		
		executor.execute(ns);		
		ns.waitUntilReady();
		
		executor.execute(np);
		np.waitUntilReady();			
		np.waitForCompletion()
		
		ns.waitForCompletion()
  }
	
  test("NatsSubscriber should receive NATS messages from NatsPublisher THROUGH SPARK STREAMING") {
		
		val messages = NatsToSparkConnector
                        .receiveFromNats(classOf[String], StorageLevel.MEMORY_ONLY)
                        .withNatsURL(NATS_LOCALHOST_URL)
                        .withSubjects(DEFAULT_SUBJECT)
                        .asStreamOf(ssc)
                        
		if ((level == Level.TRACE) || (level == Level.DEBUG)) {
		  messages.print()
		}
		
		val outputSubject = DEFAULT_SUBJECT + "_OUT"
		SparkToNatsConnectorPool.newPool()
                            .withNatsURL(NATS_LOCALHOST_URL)
                            .withSubjects(outputSubject)
                            .publishToNats(messages)
    ssc.start()
    Thread.sleep(4000)
    
    checkReceptionOfNatsMessages(outputSubject)
    
    Thread.sleep(1000)
  }
	
  test("NatsSubscriber should receive NATS messages from NatsPublisher through SparkStreaming as Key/Value") {
		
		val messages = NatsToSparkConnector
                        .receiveFromNats(classOf[String], StorageLevel.MEMORY_ONLY)
                        .withNatsURL(NATS_LOCALHOST_URL)
                        .withSubjects(DEFAULT_SUBJECT)
                        .asStreamOfKeyValue(ssc)
                        
		if ((level == Level.TRACE) || (level == Level.DEBUG)) {
		  messages.print()
		  messages.groupByKey().print()
		}
		
		val out = "OUT."
		
		SparkToNatsConnectorPool.newPool()
                            .withNatsURL(NATS_LOCALHOST_URL)
                            .withSubjects(out)
                            .publishToNatsAsKeyValue(messages)
    ssc.start()
    Thread.sleep(4000)
    
		val outputSubject =  out + DEFAULT_SUBJECT ;
    checkReceptionOfNatsMessages(outputSubject)
    
    Thread.sleep(1000)
  }

  def getNatsPublisher(nbOfMessages: Integer): NatsPublisher = {
		return new StandardNatsPublisher("np", NATS_LOCALHOST_URL, DEFAULT_SUBJECT, nbOfMessages);
	}
  
  def getNatsSubscriber(outputSubject: String, nbOfMessages: Integer): NatsSubscriber = {
		return new StandardNatsSubscriber(NATS_LOCALHOST_URL, outputSubject + "_id", outputSubject, nbOfMessages);
	}
}