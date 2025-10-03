package com.meineliebe

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.meineliebe.Order.orderArb
import com.meineliebe.Streaming.topic
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer, KafkaConsumer, KafkaProducer, ProducerRecord, ProducerRecords, ProducerSettings, Serializer}
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.scalacheck.{Arbitrary, Gen}
import io.circe.parser.decode
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.classic.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

import java.time.Instant
import scala.concurrent.duration.DurationInt

case class Order(ticker: String, amount: Long, price: Double, time: Instant)

object Order {
  val tickers = Vector(
    "S&P500",
    "CSPX",
    "DAXEX",
    "IMAE",
    "AAPL",
    "PEPSI",
    "PZU",
    "CON"
  )

  val orderArb: Arbitrary[Order] = Arbitrary(
    for {
      ticker <- Gen.oneOf(tickers)
      amount <- Gen.poisson(50).suchThat(_ > 0)
      price <- Gen.gaussian(1000, 200)
      time <- Gen.const(Instant.now())
    } yield Order(ticker, amount, price, time)
  )

  implicit val serializer: Serializer[IO, Order] = {
    Serializer.lift[IO, Order](event => IO(event.asJson.noSpaces.getBytes("UTF-8")))
  }

  implicit val deserializer: Deserializer[IO, Order] = {
    Deserializer.lift[IO, Order](event =>
      IO(decode[Order](new String(event, "UTF-8")).getOrElse(throw new Exception("Could not deserialize")))
    )
  }
}

object Produce {
  def main(args: Array[String]): Unit = {

    val producerSettings =
      ProducerSettings[IO, Unit, Order]
        .withBootstrapServers("localhost:9092")

    val consumerSettings =
      ConsumerSettings[IO, Unit, Order]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("localhost:9092")
        .withGroupId("group")

    fs2.Stream
      .fixedRateStartImmediately[IO](10.milli)
      .flatMap(_ => fs2.Stream.emits(orderArb.arbitrary.sample.toSeq))
      .map(event => ProducerRecords.one(ProducerRecord(topic, (), event)))
      .through(KafkaProducer.pipe(producerSettings))
      .compile
      .drain
      .unsafeRunSync()
  }
}

object Streaming {
  val topic = "topic"

  def main(args: Array[String]): Unit = {

    val spark = SparkSession
      .builder()
      .appName("Market")
      .master("local[4]")
      .getOrCreate()

    import spark.implicits._

    val eventSchema = ScalaReflection.schemaFor[Order].dataType.asInstanceOf[StructType]

    val ds = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", topic)
      .option("startingOffsets", "earliest")
      .load()
      .select(from_json($"value".cast("string"), eventSchema).as("event"))
      .select("event.*")
      .as[Order]
      .groupBy(
        window($"time", "1 hour", "1 hour"),
        $"ticker"
      ).agg(
        count($"ticker"),
        avg($"price"),
        percentile_approx($"price", lit(0.9), lit(1000)).as("p90"),
        percentile_approx($"price", lit(0.1), lit(1000)).as("p10"),
      )

    val consoleWriter = ds.writeStream
      .format("console")
      .outputMode(OutputMode.Update())
      .option("truncate", "false")
      .start()

    consoleWriter.awaitTermination()
  }
}
