package com.meineliebe

import com.meineliebe.Common.{extractionOptions, extractRoot, loadOptions, loadRoot}
import org.apache.spark.sql.classic.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.SaveMode

case class Data(name: String, age: Int)

object App {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder()
      .appName("App")
      .master("local[4]")
      .getOrCreate()

    import spark.implicits._

    val apps =
      spark.read
        .options(extractionOptions)
        .csv(s"$extractRoot/app/googleplaystore.csv")
        .select("App", "Rating")
        .na.drop(Seq("Rating"))

    val reviews =
      spark.read
        .options(extractionOptions)
        .csv(s"$extractRoot/app/googleplaystore_user_reviews.csv")
        .select("App", "Translated_Review", "Sentiment_Polarity")
        .withColumn(
          "Sentiment_Polarity",
          $"Sentiment_Polarity".try_cast(DoubleType)
        )
        .na.drop(Seq("Sentiment_Polarity"))

    val joined =
      apps
        .join(reviews, apps("App") === reviews("App"), "left")
        .groupBy(apps("App"), apps("Rating"))
        .agg(
          count($"Translated_Review").as("Total_Reviews"),
          median($"Sentiment_Polarity").as("Median_Polarity"),
          avg($"Sentiment_Polarity").as("Average_Polarity"),
        )
        .withColumns(
          Map(
            "Median_Polarity" -> round($"Median_Polarity", 2),
            "Average_Polarity" -> round($"Average_Polarity", 2),
          )
        )
        .filter($"Total_Reviews" > 32)
        .orderBy($"Total_Reviews".desc)
        .cache()

    joined.orderBy($"Rating".desc).show(truncate = false)
    joined.orderBy($"Median_Polarity".desc).show(truncate = false)
    joined.orderBy($"Average_Polarity".desc).show(truncate = false)

    joined
      .coalesce(1)
      .write
      .options(loadOptions)
      .mode(SaveMode.Overwrite)
      .csv(s"$loadRoot/app-out/avg-median")
	}
}
