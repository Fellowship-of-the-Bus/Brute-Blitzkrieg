package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set

import lib.game.{IDMap, IDFactory}

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class Tile(var floorTrapID: TrapID, var wallTrapID: TrapID) {
  val bruteList: Set[BaseBrute] = Set[BaseBrute]()

  def register(e: BaseBrute): Unit = {
    bruteList += e
    ()
  }

  def deregister(e: BaseBrute): Unit = {
    bruteList -= e
    ()
  }
}


case class MapInfo(
  tiles: List[List[Tile]],
  startingGold: Int,
  oneStar: Int,
  twoStar: Int,
  threeStar: Int) {
  require(height == MapID.height)
  require(width == MapID.width)

  val startTileCoord = Coordinate(7, 3)
  val endTileCoord = Coordinate(7, 0)

  def height = tiles.length
  def width = tiles(0).length
  def getTile(coord: Coordinate) = {
    //android.util.Log.e("bruteb", s"${coord.y}, ${coord.x}, $height, $width")
    tiles(coord.y.toInt)(coord.x.toInt)
  }

  def clear() = {
    for (row <- tiles) {
      for (t <- row) {
        t.bruteList.clear
      }
    }
  }
}


sealed trait MapID {
  val id: String
}
case object Level1 extends MapID {
  val id = "level1"
}

object MapID {
  val height = 4
  val width = 8

  implicit object Factory extends IDFactory[MapID] {
    val ids = Vector(Level1)
  }
  implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))

  def fromInt(i: Int): MapID = Factory.ids(i-1)
}

case object maps extends IDMap[MapID, MapInfo]("data/maps.json")

// tiles have coordinates with bottom left = (0,0)
// class LevelMap() {
//   def apply(num: Int) = levelMap(num)
//   val levelMap = collection.immutable.HashMap(1 ->
//   MapInfo(List(List(Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0)),
//                List(Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0))
//                ),
//           Coordinate(0,4), Coordinate(1,4)
//           )
//   )
// }

class GameMap(num : MapID) {
  val brute = Brute(OgreID, Coordinate(0,0))
  val info = maps(num)
  val tiles = info.tiles
  val startTileCoord = info.startTileCoord
  val endTileCoord = info.endTileCoord

  def getTile(coord: Coordinate) = {
    tiles(coord.y.toInt)(coord.x.toInt)
  }
}
