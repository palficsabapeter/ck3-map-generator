package com.palficsabapeter.ck3.mapgenerator.transformer

import better.files.File
import com.palficsabapeter.ck3.mapgenerator.terrain.TerrainHelper
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import com.sksamuel.scrimage.nio.PngWriter

object ColorTransformer {
  def transform(inputImage: File, outputFile: File) = {
    val image = ImmutableImage.loader().fromFile(inputImage.toJava)

    val transformedImage = image.map { pixel =>
      val luminosity = TerrainHelper.findTerrainByRgb(pixel.red(), pixel.green(), pixel.blue()).fold(0)(_.luminosity)
      new RGBColor(luminosity, luminosity, luminosity).toAWT
    }

    transformedImage.output(PngWriter.MaxCompression, outputFile.toJava)
  }
}
