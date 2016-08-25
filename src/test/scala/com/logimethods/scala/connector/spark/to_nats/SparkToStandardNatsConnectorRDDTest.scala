package com.logimethods.scala.connector.spark.to_nats

import org.apache.hadoop.mapred.InvalidInputException
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.ClockWrapper
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.io.Path
import scala.util.Try
import com.logimethods.connector.nats.spark.test.UnitTestUtilities;
import scala.collection.JavaConversions._

// @see https://www.supergloo.com/fieldnotes/spark-streaming-testing-scala/
class SparkToStandardNatsConnectorRDDTest extends FlatSpec with Matchers with Eventually with BeforeAndAfter {
  private val master = "local[1]"
  private val appName = "spark-streaming-test"
  private val filePath: String = "target/testfile"
  
  private val NATS_SERVER_PORT = 4221;
	private val NATS_SERVER_URL = "nats://localhost:"+NATS_SERVER_PORT;

	private val subject1 = "subject1";
	private val subject2 = "subject2";
 
  private var ssc: StreamingContext = _
  private var pool: SparkToStandardNatsConnectorPoolScala  = _
 
  private val batchDuration = Seconds(1)
 
  var clock: ClockWrapper = _
 
  before {
		// Enable tracing for debugging as necessary.
    import org.apache.log4j.Level;
		val level = Level.WARN;
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToNatsConnectorPool[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToNatsConnector[Object]], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.spark.to_nats.SparkToStandardNatsConnectorImpl], level);
		UnitTestUtilities.setLogLevel(classOf[com.logimethods.connector.nats.spark.test.TestClient], level);
		UnitTestUtilities.setLogLevel("org.apache.spark", Level.WARN);
		UnitTestUtilities.setLogLevel("org.spark-project", Level.WARN);

		val conf = new SparkConf()
      .setMaster(master).setAppName(appName)
      .set("spark.streaming.clock", "org.apache.spark.streaming.util.ManualClock")
 
    ssc = new StreamingContext(conf, batchDuration)
    clock = new ClockWrapper(ssc)
    
    pool = SparkToNatsConnectorPool.newPool().withSubjects(subject1, subject2).withNatsURL(NATS_SERVER_URL)
    
		UnitTestUtilities.startDefaultServer();
  }
 
  after {
    if (ssc != null) {
      ssc.stop()
    }
  }
 
  "SparkToNatsConnectorPool " should " send data into NATS" in {
		val data = UnitTestUtilities.getData();

		val ns1 = UnitTestUtilities.getStandardNatsSubscriber(data, subject1, NATS_SERVER_URL);
		val ns2 = UnitTestUtilities.getStandardNatsSubscriber(data, subject2, NATS_SERVER_URL);

		val lines = mutable.Queue[RDD[String]]()
    val dstream = ssc.queueStream(lines)
 
    dstream.print()
    pool.publishToNats(dstream)
 
    ssc.start()
 
    lines += ssc.sparkContext.makeRDD(data.seq)
    clock.advance(1000)
    
		// wait for the subscribers to complete.
		ns1.waitForCompletion();
		ns2.waitForCompletion();
 
/**    eventually(timeout(2 seconds)){
    }**/ 
  }
}