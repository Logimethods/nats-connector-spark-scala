# (Scala based) NATS / Spark Connectors
That library provides a Scala based [Apache Spark](http://spark.apache.org/) (a fast and general engine for large-scale data processing) integration with the [NATS messaging system](https://nats.io) (a highly performant cloud native messaging system) as well as [NATS Streaming](http://www.nats.io/documentation/streaming/nats-streaming-intro/) (a data streaming system powered by NATS).

That library provided a wrapper over the [(Java based) NATS / Spark Connectors](https://github.com/Logimethods/nats-connector-spark) to facilitate its usage on Scala (which is the *de facto* language of Spark).

**Please refer to that [page](https://github.com/Logimethods/nats-connector-spark) for additional information.**

[![MIT License](https://img.shields.io/npm/l/express.svg)](http://opensource.org/licenses/MIT)
[![Issues](https://img.shields.io/github/issues/Logimethods/nats-connector-spark-scala.svg)](https://github.com/Logimethods/nats-connector-spark-scala/issues)
[![wercker status](https://app.wercker.com/status/4dc494e7708dc596dfc37b65500cf70c/s/master "wercker status")](https://app.wercker.com/project/byKey/4dc494e7708dc596dfc37b65500cf70c)
[![Javadoc](http://javadoc-badge.appspot.com/com.logimethods/nats-connector-spark-scala.svg?label=scaladoc)](http://logimethods.github.io/nats-connector-spark-scala/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.logimethods/nats-connector-spark-scala/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.logimethods/nats-connector-spark-scala)

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

