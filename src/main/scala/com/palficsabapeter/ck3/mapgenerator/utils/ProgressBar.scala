package com.palficsabapeter.ck3.mapgenerator.utils

object ProgressBar {
  def printProgressBar(current: Int, total: Int, length: Int = 20): Unit = {
    val percent = (current.toFloat / total.toFloat * 100).toInt
    val filledLength = (length * current / total).toInt
    val bar = "=" * filledLength + "-" * (length - filledLength)
    print(f"\r|$bar| $percent%%")
    if (current == total) println()
  }
}
