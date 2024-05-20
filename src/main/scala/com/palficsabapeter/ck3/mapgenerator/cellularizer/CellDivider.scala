package com.palficsabapeter.ck3.mapgenerator.cellularizer

import better.files._
import com.palficsabapeter.ck3.mapgenerator.cellularizer.Direction._
import com.palficsabapeter.ck3.mapgenerator.terrain.{Rgb, TerrainHelper}
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.color.RGBColor
import com.sksamuel.scrimage.nio.PngWriter
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex

import scala.collection.mutable.ArrayBuffer

object CellDivider {
  private val outputFolder = File("./output/cell_divider")

  def cellularize(inputImage: File) = {
    outputFolder.createDirectoryIfNotExists(createParents = true)
    val arr = loadImage(inputImage)
    cellularizeImageArray(arr, saveCell)
  }

  private def saveCell(cell: CellData, index: Int) = {
    val cellCoordinates = cell.startCoordinates
    val filePath        = outputFolder / s"${index}_${cellCoordinates.j}_${cellCoordinates.i}_cell.png"
    println(s"Saving cell at $filePath")

    ImmutableImage
      .create(cell.width, cell.height)
      .map { pixel =>
        val realI = pixel.y + cell.startCoordinates.i
        val realJ = pixel.x + cell.startCoordinates.j
        val rgb =
          if (cell.cell.contains(Coordinates(realI, realJ)))
            Rgb(255, 255, 255)
          else Rgb(0, 0, 0)
        new RGBColor(rgb.red, rgb.green, rgb.blue).toAWT
      }
      .output(PngWriter.MaxCompression, filePath.toJava)
  }

  private def loadImage(inputImage: File) = {
    val image  = ImmutableImage.loader().fromFile(inputImage.toJava)
    val height = image.height
    val width  = image.width

    // Create an INDArray with shape (height, width, 4)
    val arr: INDArray = Nd4j.zeros(height, width, 4)

    for (i <- 0 until height)
      for (j <- 0 until width) {
        val pixel = image.pixel(j, i)
        val red   = pixel.red()
        val green = pixel.green()
        val blue  = pixel.blue
        val rgb   = Rgb(red, green, blue)

        arr.putScalar(Array(i, j, 0), red)
        arr.putScalar(Array(i, j, 1), green)
        arr.putScalar(Array(i, j, 2), blue)

        if (!isProcessablePixel(rgb))
          arr.putScalar(Array(i, j, 3), 1)
        else
          arr.putScalar(Array(i, j, 3), 0)
      }

    arr
  }

  private def isProcessablePixel(rgb: Rgb): Boolean =
    TerrainHelper.landTerrains.exists { terrain =>
      val terrainRgb = terrain.colorCode
      terrainRgb.red == rgb.red && terrainRgb.green == rgb.green && terrainRgb.blue == rgb.blue
    }

  private def checkForUpWay(i: Int, j: Int, pixels: INDArray, height: Int): Boolean =
    if (i + 1 < height) {
      val nextPixel = pixels.get(NDArrayIndex.point(i + 1), NDArrayIndex.point(j))
      val rgb       = Rgb(nextPixel.getInt(0), nextPixel.getInt(1), nextPixel.getInt(2))
      isProcessablePixel(rgb) && nextPixel.getInt(3) != 1
    } else false

  private def checkForDownWay(i: Int, j: Int, pixels: INDArray): Boolean =
    if (i - 1 >= 0) {
      val nextPixel = pixels.get(NDArrayIndex.point(i - 1), NDArrayIndex.point(j))
      val rgb       = Rgb(nextPixel.getInt(0), nextPixel.getInt(1), nextPixel.getInt(2))
      isProcessablePixel(rgb) && nextPixel.getInt(3) != 1
    } else false

  private def checkForLeftWay(i: Int, j: Int, pixels: INDArray): Boolean =
    if (j - 1 >= 0) {
      val nextPixel = pixels.get(NDArrayIndex.point(i), NDArrayIndex.point(j - 1))
      val rgb       = Rgb(nextPixel.getInt(0), nextPixel.getInt(1), nextPixel.getInt(2))
      isProcessablePixel(rgb) && nextPixel.getInt(3) != 1
    } else false

  private def checkForRightWay(i: Int, j: Int, pixels: INDArray, width: Int): Boolean =
    if (j + 1 < width) {
      val nextPixel = pixels.get(NDArrayIndex.point(i), NDArrayIndex.point(j + 1))
      val rgb       = Rgb(nextPixel.getInt(0), nextPixel.getInt(1), nextPixel.getInt(2))
      isProcessablePixel(rgb) && nextPixel.getInt(3) != 1
    } else false

  private def checkForDirection(i: Int, j: Int, pixels: INDArray, width: Int, height: Int, isFirst: Boolean = false): Option[Direction] = {
    if (i == 341 && j == 2113) {
      println("itt")
    }
    if (j >= 0 && j < width && i >= 0 && i < height)
      if (checkForRightWay(i, j, pixels, width))
        return Some(Direction.Right)
      else if (checkForUpWay(i, j, pixels, height))
        return Some(Direction.Up)
      else if (checkForLeftWay(i, j, pixels))
        return Some(Direction.Left)
      else if (checkForDownWay(i, j, pixels))
        return Some(Direction.Down)
      else if (!isFirst)
        return Some(Direction.Back)
    None
  }

  private def stepBackAndRemoveDirection(i: Int, j: Int, prevDir: Direction): (Int, Int) =
    prevDir match {
      case Direction.Right => (i, j - 1)
      case Direction.Up    => (i - 1, j)
      case Direction.Left  => (i, j + 1)
      case Direction.Down  => (i + 1, j)
      case _               => (-1, -1)
    }

  private def dfs(pixels: INDArray, i: Int, j: Int, width: Int, height: Int): DfsResult = {
    var cell              = ArrayBuffer[Coordinates]()
    var minI              = i
    var maxI              = i
    var minJ              = j
    var maxJ              = j
    var earlierDirections = ArrayBuffer[Direction.Value]()
    var currentI          = i
    var currentJ          = j

    cell.append(Coordinates(currentI, currentJ))
    pixels.putScalar(Array(currentI, currentJ, 3), 1)

    var canGoDirection = checkForDirection(currentI, currentJ, pixels, width, height, true)

    while (canGoDirection.isDefined) {
      if (pixels.getInt(currentI, currentJ, 3) == 0) {
        if (currentI < minI) minI = currentI
        if (currentI > maxI) maxI = currentI
        if (currentJ < minJ) minJ = currentJ
        if (currentJ > maxJ) maxJ = currentJ
        pixels.putScalar(Array(currentI, currentJ, 3), 1)
      }
      if (!cell.contains(Coordinates(currentI, currentJ)))
        cell.append(Coordinates(currentI, currentJ))

      canGoDirection.map(direction => if (direction != Back) earlierDirections += direction)

      canGoDirection match {
        case Some(Right) =>
          currentJ += 1
          canGoDirection = checkForDirection(currentI, currentJ, pixels, width, height)
        case Some(Up) =>
          currentI += 1
          canGoDirection = checkForDirection(currentI, currentJ, pixels, width, height)
        case Some(Left) =>
          currentJ -= 1
          canGoDirection = checkForDirection(currentI, currentJ, pixels, width, height)
        case Some(Down) =>
          currentI -= 1
          canGoDirection = checkForDirection(currentI, currentJ, pixels, width, height)
        case Some(Back) =>
          if (earlierDirections.nonEmpty) {
            val prevDirection = earlierDirections.last
            earlierDirections = earlierDirections.dropRight(1)
            val coordinates = stepBackAndRemoveDirection(currentI, currentJ, prevDirection)
            currentI = coordinates._1
            currentJ = coordinates._2
            canGoDirection = checkForDirection(currentI, currentJ, pixels, width, height)
          } else
            canGoDirection = None
        case None => canGoDirection = None
      }
    }

    DfsResult(pixels, Some(CellData(cell, Coordinates(minI, minJ), maxJ - minJ + 1, maxI - minI + 1)))
  }

  private def discoverByDfs(pixels: INDArray, i: Int, j: Int, width: Int, height: Int): DfsResult = {
    val rgb = Rgb(pixels.getInt(i, j, 0), pixels.getInt(i, j, 1), pixels.getInt(i, j, 2))
    if (isProcessablePixel(rgb))
      dfs(pixels, i, j, width, height)
    else
      DfsResult(pixels, None)
  }

  private def skipWhileNotProcessable(pixelRow: INDArray, j: Int, width: Int): Int = {
    var currentJ = j
    var rgb      = Rgb(pixelRow.getInt(currentJ, 0), pixelRow.getInt(currentJ, 1), pixelRow.getInt(currentJ, 2))
    while (currentJ + 1 < width && (pixelRow.getInt(currentJ, 3) == 1 || !isProcessablePixel(rgb))) {
      currentJ += 1
      rgb = Rgb(pixelRow.getInt(currentJ, 0), pixelRow.getInt(currentJ, 1), pixelRow.getInt(currentJ, 2))
    }
    currentJ
  }

  private def cellularizeImageArray(pixels: INDArray, saveCellFunc: (CellData, Int) => Unit): Unit = {
    println("Cellularizing image array")
    var pixelArray = pixels
    val height     = pixels.shape()(0).toInt
    val width      = pixels.shape()(1).toInt
    var ctr        = 1

    for (i <- 0 until height) {
      var j = 0
      while (j + 1 < width) {
        j = skipWhileNotProcessable(pixelArray.get(NDArrayIndex.point(i)), j, width)
        val dfsResult = discoverByDfs(pixelArray, i, j, width, height)
        pixelArray = dfsResult.pixelArray
        dfsResult.cellData.map { cell =>
          println(s"Index: $ctr, new cell found at ${cell.startCoordinates}")
          saveCellFunc(cell, ctr)
          ctr += 1
        }
      }
    }
  }

  final case class Coordinates(
      i: Int,
      j: Int,
  )

  final case class CellData(
      cell: ArrayBuffer[Coordinates],
      startCoordinates: Coordinates,
      width: Int,
      height: Int,
  )

  final case class DfsResult(pixelArray: INDArray, cellData: Option[CellData])
}
