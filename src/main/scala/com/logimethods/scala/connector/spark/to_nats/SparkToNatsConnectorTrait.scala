/*******************************************************************************
 * Copyright (c) 2016 Logimethods
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *******************************************************************************/
package com.logimethods.connector.spark.to_nats

import org.apache.spark.streaming.dstream.DStream

trait SparkToNatsConnectorPoolTrait[T] extends SparkToNatsConnectorPool[T] {

  // http://spark.apache.org/docs/1.6.2/streaming-programming-guide.html#design-patterns-for-using-foreachrdd
  def publishToNats(rdd: DStream[_]){
    rdd.foreachRDD { rdd1 =>
      rdd1.foreachPartition { partitionOfRecords =>
			  val connector = getConnector();
        partitionOfRecords.foreach(record => connector.publish(record))
        returnConnector(connector)  // return to the pool for future reuse
      }
    }
  }
}