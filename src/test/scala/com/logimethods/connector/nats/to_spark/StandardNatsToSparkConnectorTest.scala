package com.logimethods.connector.nats.to_spark

import com.logimethods.scala.connector.spark.to_nats._

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.mapred.InvalidInputException
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.ClockWrapper
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
import com.logimethods.connector.nats_spark.Utilities;

import com.logimethods.connector.nats.spark.test.NatsPublisher;
import com.logimethods.connector.nats.spark.test.StandardNatsPublisher;
import com.logimethods.connector.nats.spark.test.StandardNatsSubscriber;

class StandardNatsToSparkConnectorTest extends FunSuite with BeforeAndAfter with TimeLimitedTests { //FlatSpec with Matchers with Eventually with BeforeAndAfter {
  // http://doc.scalatest.org/3.0.0/index.html#org.scalatest.concurrent.TimeLimitedTests
  def timeLimit = 15 second
  override val defaultTestSignaler = ThreadSignaler

	val DEFAULT_SUBJECT_ROOT = "nats2sparkSubject"
	var DEFAULT_SUBJECT_INR = 0
	var DEFAULT_SUBJECT: String = ""

	private val master = "local[3]"
  private val appName = "spark-streaming-test"
  private val filePath: String = "target/testfile"

	private val subject1 = "subject1";
	private val subject2 = "subject2";
	
  private var ssc: StreamingContext = _
  private var pool: SparkToNatsStreamingConnectorPoolScala  = _
	
  private val batchDuration = Seconds(1)
 
  var clock: ClockWrapper = _

  before {
		// Enable tracing for debugging as necessary.
    import org.apache.log4j.Level;
		val level = Level.TRACE;
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToNatsConnectorPool[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToNatsConnector[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToStandardNatsConnectorImpl], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.StandardNatsPublisher], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.NatsPublisher], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.StandardNatsSubscriber], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.NatsSubscriber], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.TestClient], level);
		UnitTestUtilities.setLogLevel("org.apache.spark", Level.WARN);
		UnitTestUtilities.setLogLevel("org.spark-project", Level.WARN);

		val conf = new SparkConf()
      .setMaster(master).setAppName(appName)
      .set("spark.streaming.clock", "org.apache.spark.streaming.util.ManualClock")
 
    ssc = new StreamingContext(conf, batchDuration)
    clock = new ClockWrapper(ssc)
    
    pool = SparkToNatsConnectorPool.newStreamingPool(CLUSTER_ID).withSubjects(subject1, subject2).withNatsURL(STAN_URL)
    
		UnitTestUtilities.startStreamingServer(CLUSTER_ID);
		
		DEFAULT_SUBJECT_INR += 1
		DEFAULT_SUBJECT = DEFAULT_SUBJECT_ROOT + DEFAULT_SUBJECT_INR;
  }
 
  after {
    if (ssc != null) {
      ssc.stop()
    }
  }
	
  test("NatsSubscriber should receive NATS messages DIRECTLY from NatsPublisher") {
		val nsExecutor = Executors.newFixedThreadPool(6);
		val npExecutor = Executors.newFixedThreadPool(6);

		val nbOfMessages = 5;
		val np = getNatsPublisher(nbOfMessages);

		val ns: StandardNatsSubscriber = new StandardNatsSubscriber(NATS_SERVER_URL, DEFAULT_SUBJECT + "_id", DEFAULT_SUBJECT, nbOfMessages);

		nsExecutor.execute(ns);		
		ns.waitUntilReady();
		
		npExecutor.execute(np);
		np.waitUntilReady();			
		np.waitForCompletion()
		
		ns.waitForCompletion()
  }
  
  def getNatsPublisher(nbOfMessages: Integer): NatsPublisher = {
		return new StandardNatsPublisher("np", NATS_SERVER_URL, DEFAULT_SUBJECT,  nbOfMessages);
	}

}