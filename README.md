# (Scala based) NATS / Spark Connectors
That library provides a Scala based [Apache Spark](http://spark.apache.org/) (a fast and general engine for large-scale data processing) integration with the [NATS messaging system](https://nats.io) (a highly performant cloud native messaging system) as well as [NATS Streaming](http://www.nats.io/documentation/streaming/nats-streaming-intro/) (a data streaming system powered by NATS).

That library provided a wrapper over the [(Java based) NATS / Spark Connectors](https://github.com/Logimethods/nats-connector-spark) to facilitate its usage on Scala (which is the *de facto* language of Spark).

**Please refer to that [page](https://github.com/Logimethods/nats-connector-spark) for additional information.**

[![MIT License](https://img.shields.io/npm/l/express.svg)](http://opensource.org/licenses/MIT)
[![Issues](https://img.shields.io/github/issues/Logimethods/nats-connector-spark-scala.svg)](https://github.com/Logimethods/nats-connector-spark-scala/issues)
[![wercker status](https://app.wercker.com/status/4dc494e7708dc596dfc37b65500cf70c/s/master "wercker status")](https://app.wercker.com/project/byKey/4dc494e7708dc596dfc37b65500cf70c)
[![Scaladoc](http://javadoc-badge.appspot.com/com.logimethods/nats-connector-spark-scala_2.10.svg?label=scaladoc)](http://logimethods.github.io/nats-connector-spark-scala/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.logimethods/nats-connector-spark-scala_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.logimethods/nats-connector-spark-scala_2.10)

## Installation
```Scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
resolvers += "Sonatype OSS Release" at "https://oss.sonatype.org/content/groups/public/"

libraryDependencies += "com.logimethods"  %% "nats-connector-spark-scala" % "0.2.0"
```

## Usage (in Scala)
_See the [Java code Documentation](https://github.com/Logimethods/nats-connector-spark/blob/master/README.md#usage-in-java) to get the list of the available options (properties, subjects, etc.)._
```Scala
import com.logimethods.connector.nats.to_spark._
import com.logimethods.scala.connector.spark.to_nats._

val ssc = new StreamingContext(sc, new Duration(2000));
```

### From NATS Streaming to Spark
```Scala
val stream = NatsToSparkConnector.receiveFromNatsStreaming(StorageLevel.MEMORY_ONLY, clusterId)
                                 .withNatsURL(natsUrl)
                                 .withSubjects(inputSubject)
```
### From NATS ~~Streaming~~ to Spark
```Scala
val properties = new Properties()
val natsUrl = System.getenv("NATS_URI")
val stream = NatsToSparkConnector.receiveFromNats(StorageLevel.MEMORY_ONLY)
                                 .withProperties(properties)
                                 .withSubjects(inputSubject)
```

### From Spark to NATS Streaming
```Scala
SparkToNatsConnectorPool.newStreamingPool(clusterId)
                        .withNatsURL(natsUrl)
                        .withSubjects(outputSubject)
                        .publishToNats(ssc)
```

### From Spark to NATS ~~Streaming~~
```Scala
SparkToNatsConnectorPool.newPool()
                        .withProperties(properties)
                        .withSubjects(outputSubject)
                        .publishToNats(ssc)
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
