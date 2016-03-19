package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set

import lib.game.{IDMap, IDFactory}

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class Tile(val floorTrapID: Int, val wallTrapID: Int) {
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
  startTileCoord: Coordinate, //(Int, Int),
  endTileCoord: Coordinate) //(Int, Int))

sealed trait MapID
case object Level1 extends MapID

object MapID {
  implicit object Factory extends IDFactory[MapID] {
    val ids = Vector(Level1)
    implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))
  }
  def fromInt(i: Int) = Factory.ids(i-1)
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
