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
  def getTile(coord: Coordinate) = tiles(coord.y.toInt)(coord.x.toInt)

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
  val id = "Level1"
}
case object LevelEasy extends MapID {
  val id = "LevelEasy"
}
case object LevelTrapdoor extends MapID {
  val id = "LevelTrapdoor"
}
case object LevelPoisonLightning extends MapID {
  val id = "LevelPoisonLightning"
}
case object LevelFire extends MapID {
  val id = "LevelFire"
}
case object LevelGambit extends MapID {
  val id = "LevelGambit"
}
case object LevelBullshit extends MapID {
  val id = "LevelBullshit"
}
case object LevelTiming extends MapID {
  val id = "LevelTiming"
}
object Custom {
  val prefix = "Custom_"
}
case class Custom(val name: String) extends MapID {
  val id = s"${Custom.prefix}$name"
}

object MapID {
  val height = 4
  val width = 8

  implicit object Factory extends IDFactory[MapID] {
    val ids = Vector(Level1, LevelTrapdoor,LevelEasy, LevelPoisonLightning,LevelFire,LevelGambit,LevelBullshit,LevelTiming)
  }
  implicit lazy val extractor =
    Json.extractor[String].map(x => if (Factory.fromString.isDefinedAt(x)) Factory.fromString(x) else Custom(x))
  implicit lazy val serializer =
    Json.serializer[String].contramap[MapID] { mid => mid.id }

  def fromInt(i: Int): MapID = Factory.ids(i-1)
}

case object maps extends IDMap[MapID, MapInfo]("data/maps.json")

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
