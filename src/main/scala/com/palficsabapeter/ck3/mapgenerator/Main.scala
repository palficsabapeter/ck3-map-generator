package com.palficsabapeter.ck3.mapgenerator

import better.files._
import com.palficsabapeter.ck3.mapgenerator.cellularizer.CellDivider
import com.palficsabapeter.ck3.mapgenerator.transformer.ColorTransformer
import mainargs.{ParserForMethods, arg, main}

object Main {
  @main
  def run(@arg(short = 's', doc = "The path of the source file") src: String) = {
    val inputFile    = "./input" / src
    val outputFolder = "./output".toFile
    outputFolder.createDirectoryIfNotExists(true)
    //ColorTransformer.transform(inputFile, outputFolder / "heightmap.png")
    CellDivider.cellularize(inputFile)
  }

  def main(args: Array[String]): Unit = ParserForMethods(this).runOrExit(args)
}
