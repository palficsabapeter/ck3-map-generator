package com.palficsabapeter.ck3.mapgenerator.terrain

final case class Terrain(
    colorCode: Rgb,
    luminosity: Int,
)

final case class Rgb(
    red: Int,
    green: Int,
    blue: Int,
)
