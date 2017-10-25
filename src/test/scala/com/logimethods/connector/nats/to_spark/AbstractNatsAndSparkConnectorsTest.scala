package com.logimethods.connector.nats.to_spark

import com.logimethods.scala.connector.spark.to_nats._

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.mapred.InvalidInputException
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.scalatest.FunSuite
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.concurrent.{ThreadSignaler, TimeLimitedTests}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.io.Path
import scala.util.Try
import com.logimethods.connector.nats.spark.test.UnitTestUtilities;
import com.logimethods.scala.connector.spark.to_nats._
import scala.collection.JavaConversions._
import org.apache.spark.storage.StorageLevel;

import com.logimethods.connector.nats.spark.test.UnitTestUtilities;
import com.logimethods.connector.nats.spark.test.UnitTestUtilities._;
import scala.collection.JavaConversions._

import com.logimethods.connector.nats.spark.test.NatsPublisher;
import com.logimethods.connector.nats.spark.test.NatsSubscriber;

abstract class AbstractNatsAndSparkConnectorsTest extends FunSuite with BeforeAndAfter with TimeLimitedTests { //FlatSpec with Matchers with Eventually with BeforeAndAfter {
  // http://doc.scalatest.org/3.0.0/index.html#org.scalatest.concurrent.TimeLimitedTests
  def timeLimit = 20 second
  override val defaultTestSignaler = ThreadSignaler

	val DEFAULT_SUBJECT_ROOT = "nats2spark"
	var DEFAULT_SUBJECT_INR = 0
	var DEFAULT_SUBJECT: String = ""

	val master = "local[3]"
  val appName = "spark-streaming-test"
  val filePath: String = "target/testfile"

	val subject1 = "subject1";
	val subject2 = "subject2";
	
  var ssc: StreamingContext = _
  var pool: SparkToNatsStreamingConnectorPoolScala  = _
	
  val batchDuration = Seconds(1)
  
  import org.apache.log4j.Level;
	val level = Level.WARN;

  before {
		// Enable tracing for debugging as necessary.
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToNatsConnectorPool[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToNatsConnector[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.AbstractSparkToStandardNatsConnectorPool[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.to_spark.StandardNatsToSparkConnectorImpl[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.StandardNatsPublisher], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.NatsPublisher], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.StandardNatsSubscriber], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.NatsSubscriber], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.TestClient], level);
		UnitTestUtilities.setLogLevel(classOf[org.spark_project.jetty.server.handler.ContextHandler], level);
		
		UnitTestUtilities.setLogLevel("org.apache.spark", Level.WARN);
		UnitTestUtilities.setLogLevel("org.spark-project", Level.WARN);

		val conf = new SparkConf().setMaster(master)
                      				.setAppName(appName)
                      				.set("spark.driver.host", "localhost") // https://issues.apache.org/jira/browse/SPARK-19394
 
    ssc = new StreamingContext(conf, batchDuration)
    
		UnitTestUtilities.startStreamingServer(CLUSTER_ID);
		
		DEFAULT_SUBJECT_INR += 1
		DEFAULT_SUBJECT = DEFAULT_SUBJECT_ROOT + DEFAULT_SUBJECT_INR;
  }
 
  after {
    if (ssc != null) {
      ssc.stop()
    }
  }

  // TESTS to add
  
  def checkReceptionOfNatsMessages(outputSubject: String) = {
		val executor = Executors.newFixedThreadPool(12);
		
    val nbOfMessages = 5;
		val np = getNatsPublisher(nbOfMessages);

		val ns: NatsSubscriber = getNatsSubscriber(outputSubject, nbOfMessages)

		executor.execute(ns);		
		ns.waitUntilReady();
		
		executor.execute(np);
		np.waitUntilReady();			
		np.waitForCompletion()
		
		ns.waitForCompletion()
  }
  
  def getNatsPublisher(nbOfMessages: Integer): NatsPublisher
  
  def getNatsSubscriber(outputSubject: String, nbOfMessages: Integer): NatsSubscriber

}