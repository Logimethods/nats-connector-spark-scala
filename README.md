# nats-connector-spark-scala
A Scala based Spark Publish/Subscribe NATS Connector

## Usage (in Scala)  *WARNING: NEED TO BE UPDATED TO VERSION 0.2.0*
_See the Java code to get the list of the available options (properties, subjects, etc.)._
### From NATS to Spark (Streaming)
```
val messages = ssc.receiverStream(NatsToSparkConnector.receiveFromNats(properties, StorageLevel.MEMORY_ONLY, inputSubject))
```

### From Spark (Streaming) to NATS
See [Design Patterns for using foreachRDD](http://spark.apache.org/docs/latest/streaming-programming-guide.html#design-patterns-for-using-foreachrdd)
```
messages.foreachRDD { rdd =>
  val connectorPool = new SparkToNatsConnectorPool(properties, outputSubject)
  rdd.foreachPartition { partitionOfRecords =>
    val connector = connectorPool.getConnector()
    partitionOfRecords.foreach(record => connector.publishToNats(record))
    connectorPool.returnConnector(connector)  // return to the pool for future reuse
  }
}
```

### From Spark (*WITHOUT Streaming NOR Spark Cluster*) to NATS
```
import com.logimethods.nats.connector.spark.SparkToNatsConnector;
```
```
val publishToNats = SparkToNatsConnector.publishToNats(properties, outputSubject)
messages.foreachRDD { rdd => rdd.foreach { m => publishToNats.call(m.toString()) }}
```

