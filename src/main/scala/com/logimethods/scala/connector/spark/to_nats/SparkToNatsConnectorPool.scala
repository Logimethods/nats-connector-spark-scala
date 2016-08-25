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
                                            with SparkToNatsConnectorPoolTrait[SparkToStandardNatsConnectorPoolScala] 
{  
}

object SparkToNatsConnectorPool  {  
  def newPool(): SparkToStandardNatsConnectorPoolScala = {
		new SparkToStandardNatsConnectorPoolScala()
	}
}
