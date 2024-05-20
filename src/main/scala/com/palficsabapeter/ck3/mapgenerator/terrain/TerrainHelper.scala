package com.palficsabapeter.ck3.mapgenerator.terrain

object TerrainHelper {
  val floodplain = Terrain(Rgb(55, 31, 153), 18)
  val wetland    = Terrain(Rgb(76, 153, 153), 18)
  val oasis      = Terrain(Rgb(155, 143, 204), 18)

  val floodlands = Seq(floodplain, wetland, oasis)

  val plain            = Terrain(Rgb(204, 163, 102), 20)
  val farmland         = Terrain(Rgb(255, 0, 0), 20)
  val steppe           = Terrain(Rgb(200, 100, 25), 20)
  val dryland          = Terrain(Rgb(220, 45, 120), 20)
  val desert           = Terrain(Rgb(255, 229, 0), 20)
  val impassableDesert = Terrain(Rgb(255, 180, 30), 20)
  val forest           = Terrain(Rgb(71, 178, 45), 21)
  val jungle           = Terrain(Rgb(10, 60, 35), 21)
  val taiga            = Terrain(Rgb(46, 153, 89), 21)

  val lowlands = floodlands ++ Seq(plain, farmland, steppe, dryland, desert, impassableDesert, forest, jungle, taiga)

  val hills               = Terrain(Rgb(90, 50, 12), 30)
  val mountains           = Terrain(Rgb(100, 100, 100), 60)
  val desertMountains     = Terrain(Rgb(23, 19, 38), 55)
  val impassableMountains = Terrain(Rgb(36, 36, 36), 75)

  val highlands = Seq(hills, mountains, desertMountains, impassableMountains)

  val landTerrains = lowlands ++ highlands

  val river         = Terrain(Rgb(142, 232, 255), 7)
  val sea           = Terrain(Rgb(68, 107, 163), 5)
  val impassableSea = Terrain(Rgb(51, 67, 85), 2)

  val waterTerrains = Seq(river, sea, impassableSea)

  val allTerrains = landTerrains ++ waterTerrains

  def findTerrainByRgb(red: Int, green: Int, blue: Int): Option[Terrain] =
    allTerrains.find(_.colorCode == Rgb(red, green, blue))
}
