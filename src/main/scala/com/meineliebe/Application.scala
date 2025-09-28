package com.meineliebe

import org.apache.spark.sql.classic.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DoubleType

import java.nio.charset.Charset

case class Data(name: String, age: Int)

object Application {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("MeineLiebeApplication")
      .master("local[4]")
      .getOrCreate()

    import spark.implicits._

    val extractionOptions = Map(
      "header" -> "true",
      "inferSchema" -> "true",
      "delimiter" -> ",",
      "encoding" -> "UTF-8"
    )

    val apps =
      spark.read
        .options(extractionOptions)
        .csv(".warehouse/app/googleplaystore.csv")

    val reviews =
      spark.read
        .options(extractionOptions)
        .csv(".warehouse/app/googleplaystore_user_reviews.csv")
        .withColumn(
          "Sentiment_Polarity",
          $"Sentiment_Polarity".try_cast(DoubleType)
        )
        .filter(!$"Sentiment_Polarity".isNaN)

    val joined =
      apps
        .join(reviews, apps("App") === reviews("App"), "left")
        .groupBy(apps("App"))
        .agg(
          count($"Translated_Review").as("Total_Reviews"),
          median($"Sentiment_Polarity").as("Median_Polarity"),
          avg($"Sentiment_Polarity").as("Average_Polarity")
        )
        .withColumns(
          Map(
            "Median_Polarity" -> round($"Median_Polarity", 2),
            "Average_Polarity" -> round($"Average_Polarity", 2),
          )
        )
        .orderBy($"Total_Reviews".desc)
        .filter($"Total_Reviews" > 32)
        .cache()

    joined.orderBy($"Median_Polarity".desc).show(truncate = false)
    joined.orderBy($"Average_Polarity".desc).show(truncate = false)
  }
}
