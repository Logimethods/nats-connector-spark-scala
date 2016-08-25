package com.logimethods.scala.connector.spark.to_nats

import collection.mutable.Stack
import org.scalatest._

class SparkToNatsConnectorPoolTest extends FunSuite {
  
  test("SparkToNatsConnectorPool.newPool() should return an instance of SparkToStandardNatsConnectorPoolScala") {
    val pool = SparkToNatsConnectorPool.newPool()
    assert(pool.isInstanceOf[SparkToStandardNatsConnectorPoolScala])
  }
  
}