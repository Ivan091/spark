package com.meineliebe

import org.apache.spark.sql.classic.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

import java.sql.Date

object Market extends Common {

  // noinspection ScalaWeakerAccess
  case class Item(
    ticker: String,
    date: Date,
    open: Double,
    high: Double,
    low: Double,
    close: Double,
    adjClose: Double,
    volume: Long
  )
  // noinspection ScalaWeakerAccess
  case class ItemShort(ticker: String, date: Date, adjClose: Double)

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("Market")
      .master("local[4]")
      .getOrCreate()

    import spark.implicits._

    spark.read
      .parquet(s"$extractRoot/market")
      .na
      .drop()
      .filter($"ticker".equalTo("AA"))
      .orderBy($"date".asc)
      .as[ItemShort]
      .withColumn("prevAdjClose", lag($"adjClose", 1).over(Window.partitionBy($"ticker").orderBy($"date".desc)))
      .withColumn(
        "change",
        when($"adjClose" > $"prevAdjClose", "UP").when($"adjClose" < $"prevAdjClose", "Down").otherwise("Equal")
      )
      .show()
  }
}
