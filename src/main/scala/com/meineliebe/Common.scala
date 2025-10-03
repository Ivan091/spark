package com.meineliebe

trait Common {
  val extractRoot = ".data"
  val loadRoot = ".warehouse"

  val extractionOptions = Map(
    "header" -> "true",
    "encoding" -> "UTF-8",
    "delimiter" -> ",",
    "escape" -> "\"",
    "multiLine" -> "true"
  )

  val loadOptions = Map(
    "header" -> "true",
    "encoding" -> "UTF-8"
  )
}

object Common extends Common
