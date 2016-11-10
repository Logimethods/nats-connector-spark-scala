# (Scala based) NATS / Spark Connectors
That library provides a Scala based [Apache Spark](http://spark.apache.org/) (a fast and general engine for large-scale data processing) integration with the [NATS messaging system](https://nats.io) (a highly performant cloud native messaging system) as well as [NATS Streaming](http://www.nats.io/documentation/streaming/nats-streaming-intro/) (a data streaming system powered by NATS).

That library provided a wrapper over the [(Java based) NATS / Spark Connectors](https://github.com/Logimethods/nats-connector-spark) to facilitate its usage on Scala (which is the *de facto* language of Spark).

**Please refer to that [page](https://github.com/Logimethods/nats-connector-spark) for additional information.**

[![MIT License](https://img.shields.io/npm/l/express.svg)](http://opensource.org/licenses/MIT)
[![Issues](https://img.shields.io/github/issues/Logimethods/nats-connector-spark-scala.svg)](https://github.com/Logimethods/nats-connector-spark-scala/issues)
[![wercker status](https://app.wercker.com/status/4dc494e7708dc596dfc37b65500cf70c/s/master "wercker status")](https://app.wercker.com/project/byKey/4dc494e7708dc596dfc37b65500cf70c)
[![Scaladoc](http://javadoc-badge.appspot.com/com.logimethods/nats-connector-spark-scala_2.10.svg?label=scaladoc)](http://logimethods.github.io/nats-connector-spark-scala/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.logimethods/nats-connector-spark-scala_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.logimethods/nats-connector-spark-scala_2.10)

## Release Notes
### Version 0.3.0-SNAPSHOT
- Spark version 2.0.1 + Scala version 1.11.8
- `.asStreamOf(ssc)` introduced
- `storedAsKeyValue()` introduced
- Message Data can be any Java `Object` (not limited to `String`), serialized as `byte[]` (the native NATS payload format)

## Installation
```Scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Sonatype OSS Release" at "https://oss.sonatype.org/content/groups/public/"

libraryDependencies += "com.logimethods"  %% "nats-connector-spark-scala" % "0.2.0"
```

## Usage (in Scala)
_See the [Java code Documentation](https://github.com/Logimethods/nats-connector-spark/blob/master/README.md#usage-in-java) to get the list of the available options (properties, subjects, encoder/decoder, etc.)._
```Scala
import com.logimethods.connector.nats.to_spark._
import com.logimethods.scala.connector.spark.to_nats._

val ssc = new StreamingContext(sc, new Duration(2000));
```
### From NATS to Spark

The reception of NATS Messages as Spark Steam is done through the `NatsToSparkConnector.receiveFromNats(classOf[Class], ...)` method, where `[Class]` is the Java Class of the objects to deserialize.

#### Deserialization of the primitive types

Those objects need first to be serialized as `byte[]` using the right protocol before being stored into the NATS messages payload.

By default, the (Java) primitive types are then automatically decoded by the connector.

#### Custom Deserialization

For more complex types, you should provide your own decoder through the `withDataDecoder(scala.Function1<byte[], V> dataDecoder)` method.

Let's say that the payload have been encoded that way:
```Scala
def encodePayload(date: LocalDateTime, value: Float): Array[Byte] = {
  // https://docs.oracle.com/javase/8/docs/api/java/nio/ByteBuffer.html
  val buffer = ByteBuffer.allocate(4+8);
  buffer.putLong(date.atZone(zoneId).toEpochSecond())
  buffer.putFloat(value)
  
  return buffer.array()    
}
```

You should provide your own decoder:

```Scala
def dataDecoder: Array[Byte] => Tuple2[Long,Float] = bytes => {
      import java.nio.ByteBuffer
      val buffer = ByteBuffer.wrap(bytes);
      val epoch = buffer.getLong()
      val voltage = buffer.getFloat()
      (epoch, voltage)  
    }

import org.apache.spark.streaming.dstream._
val messages: ReceiverInputDStream[(String, (Long, Float))] =
  NatsToSparkConnector
    .receiveFromNatsStreaming(classOf[Tuple2[Long,Float]], StorageLevel.MEMORY_ONLY, clusterId)
    .withNatsURL(natsUrl)
    .withSubjects(inputSubject)
    .withDataDecoder(dataDecoder)
    .asStreamOfKeyValue(ssc)
```

#### From NATS Streaming to Spark
```Scala
val stream = NatsToSparkConnector.receiveFromNatsStreaming(classOf[String], StorageLevel.MEMORY_ONLY, clusterId)
                                 .withNatsURL(natsUrl)
                                 .withSubjects(inputSubject)
                                 .asStreamOf(ssc)
```
#### From NATS ~~Streaming~~ to Spark
```Scala
val properties = new Properties()
val natsUrl = System.getenv("NATS_URI")
val stream = NatsToSparkConnector.receiveFromNats(classOf[Integer], StorageLevel.MEMORY_ONLY)
                                 .withProperties(properties)
                                 .withSubjects(inputSubject)
                                 .asStreamOf(ssc)
```

#### From NATS (Streaming or not) to Spark as *Key/Value Pairs*

The Spark Stream is there made of Key/Value Pairs, where the Key is the _Subject_ and the Value is the _Payload_ of the NATS Messages.

```Scala
val stream = NatsToSparkConnector.receiveFromNats[Streaming](...)
                                 ...
                                 .withSubjects("main-subject.>")
                                 .asStreamOfKeyValue(ssc)
stream.groupByKey().print()
```

### From Spark to NATS

#### Serialization of the primitive types

The Spark elements are first serialized as `byte[]` before being sent to NATS. By default, the (Java) primitive types are encoded through the `com.logimethods.connector.nats_spark.NatsSparkUtilities.encodeData(Object obj)` method.

#### Custom Serialization

Custom serialization can be performed by a `java.util.function.Function<[Class],  byte[]> & Serializable)` function provided through the `.publishToNats(...)` method, like:

```scala
val stream: DStream[(String, (Long, Float))] = .../...

def longFloatTupleEncoder: Tuple2[Long,Float] => Array[Byte] = tuple => {        
      val buffer = ByteBuffer.allocate(8+4);
      buffer.putLong(tuple._1)
      buffer.putFloat(tuple._2)        
      buffer.array()    
    }

SparkToNatsConnectorPool.newStreamingPool(clusterId)
                        .withNatsURL(natsUrl)
                        .withSubjects(outputSubject)
                        .publishToNatsAsKeyValue(stream, longFloatTupleEncoder)
```

#### From Spark to NATS Streaming
```Scala
SparkToNatsConnectorPool.newStreamingPool(clusterId)
                        .withNatsURL(natsUrl)
                        .withSubjects(outputSubject)
                        .publishToNats(stream)
```

#### From Spark to NATS ~~Streaming~~
```Scala
SparkToNatsConnectorPool.newPool()
                        .withProperties(properties)
                        .withSubjects(outputSubject)
                        .publishToNats(stream)
                        
```
#### From Spark as *Key/Value Pairs* to NATS (Streaming or not)

The Spark Stream should there be made of Key/Value Pairs. `.storedAsKeyValue()` will publish NATS Messages where the Subject is a composition of the (optional) _Global Subject(s)_ and the _Key_ of the Pairs ; while the NATS _Payload_ will be the Pair's _Value_.

```
stream.groupByKey().print()

SparkToNatsConnectorPool.new[Streaming]Pool(...)
                        ...
                        .withSubjects("A1.", "A2.")
                        .publishToNatsAsKeyValue(stream)
```
will send to NATS such [subject:payload] messages:
```
[A1.key1:string1]
[A2.key1:string1]
[A1.key2:string2]
[A2.key2:string2]
...
```
## Code Samples
* The ['docker-nats-connector-spark'](https://github.com/Logimethods/docker-nats-connector-spark) Docker Based Project that makes use of Gatling, Spark & NATS.

## License

(The MIT License)

Copyright (c) 2016 Logimethods.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to
deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
IN THE SOFTWARE.
