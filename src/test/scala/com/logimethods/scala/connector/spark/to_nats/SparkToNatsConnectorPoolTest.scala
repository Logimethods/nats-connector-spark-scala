package com.logimethods.connector.spark.to_nats

import collection.mutable.Stack
import org.scalatest._
import com.logimethods.scala.connector.spark.to_nats._

class SparkToNatsConnectorPoolTest extends FunSuite {
  
  test("SparkToNatsConnectorPool.newPool() should return an instance of SparkToStandardNatsConnectorPoolScala") {
    val pool = com.logimethods.scala.connector.spark.to_nats.SparkToNatsConnectorPool.newPool()
    assert(pool.isInstanceOf[SparkToStandardNatsConnectorPoolScala])
  }
  
  test("Connector should be a SparkToStandardNatsConnectorImpl") {
    val pool = com.logimethods.scala.connector.spark.to_nats.SparkToNatsConnectorPool.newPool().withSubjects("Subject")
    val connector = pool.getConnector()
    assert(connector.isInstanceOf[SparkToStandardNatsConnectorImpl])
  }
   
  test("SparkToNatsConnectorPool.newStreamingPool(clusterID: String) should return an instance of SparkToNatsStreamingConnectorPoolScala") {
    val pool = com.logimethods.scala.connector.spark.to_nats.SparkToNatsConnectorPool.newStreamingPool("clusterID")
    assert(pool.isInstanceOf[SparkToNatsStreamingConnectorPoolScala])
  }
  
  test("Connector should be a SparkToNatsStreamingConnectorImpl") {
    val pool = com.logimethods.scala.connector.spark.to_nats.SparkToNatsConnectorPool.newStreamingPool("clusterID").withSubjects("Subject")
    val connector = pool.getConnector()
    assert(connector.isInstanceOf[SparkToNatsStreamingConnectorImpl])
  }
}