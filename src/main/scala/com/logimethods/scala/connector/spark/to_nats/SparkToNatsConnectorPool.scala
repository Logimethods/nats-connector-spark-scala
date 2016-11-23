/*******************************************************************************
 * Copyright (c) 2016 Logimethods
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the MIT License (MIT)
 * which accompanies this distribution, and is available at
 * http://opensource.org/licenses/MIT
 *******************************************************************************/
package com.logimethods.scala.connector.spark.to_nats

import org.apache.spark.streaming.dstream.DStream
import com.logimethods.connector.spark.to_nats._

class SparkToStandardNatsConnectorPoolScala extends AbstractSparkToStandardNatsConnectorPool[SparkToStandardNatsConnectorPoolScala] 
                                            with SparkToNatsConnectorPoolTrait[SparkToStandardNatsConnectorPoolScala] {  
}

class SparkToNatsStreamingConnectorPoolScala(clusterID: String) 
                                            extends AbstractSparkToNatsStreamingConnectorPool[SparkToNatsStreamingConnectorPoolScala](clusterID: String) 
                                            with SparkToNatsConnectorPoolTrait[SparkToNatsStreamingConnectorPoolScala] {  
}

/**
 * The Scala version of the (Java based) Spark to Nats Connector Pool.
 * 
 * @see <a href="https://github.com/Logimethods/nats-connector-spark-scala">(Scala based) NATS / Spark Connectors</a>
 * @see <a href="https://github.com/Logimethods/nats-connector-spark">(Java based) NATS / Spark Connectors</a>
 * 
 * @author Laurent Magnin
 */
object SparkToNatsConnectorPool  {  
   /**
   * @return a pool of Spark to NATS Connectors that should be called through the `publishToNats(stream: DStream[_], ...)` methods
   */
  def newPool(): SparkToStandardNatsConnectorPoolScala = {
		new SparkToStandardNatsConnectorPoolScala()
	}
  
   /**
   * @return a pool of Spark to NATS Streaming Connectors that should be called through the `publishToNats(stream: DStream[_], ...)` methods
   */
  def newStreamingPool(clusterID: String): SparkToNatsStreamingConnectorPoolScala = {
		new SparkToNatsStreamingConnectorPoolScala(clusterID)
	}
}
