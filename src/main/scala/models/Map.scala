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
    e.tile.deregister(e)
    e.tile = this
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
  val preHint: String = ""
  val postHint: String = ""
}
case object Level1 extends MapID {
  val id = "Level1"
}
case object LevelArrow extends MapID {
  val id = "LevelArrow"
  override val preHint = "Sending brutes as a group reduces the amount of time arrow traps fire."
  override val postHint = "Try sending an Ogre before as many goblins as possible."
}
case object LevelIntroBat extends MapID {
  val id = "LevelIntroBat"
  override val preHint = "Bats can fly up through open trapdoors."
  override val postHint = "Use a goblin to open the trapdoor for the bats."
}
case object LevelEasy extends MapID {
  val id = "LevelEasy"
  override val preHint = "Spiders can seal trapdoors permanently."
  override val postHint = "Try using a goblin before a spider to keep it alive long enough."
}
case object LevelTrapdoor extends MapID {
  val id = "LevelTrapdoor"
  override val preHint = "By not sending brutes in a group, they won't all fall through a reusable trapdoor."
  override val postHint = "Experiment with timings until you get it right."
}
case object LevelLightning extends MapID {
  val id = "LevelLightning"
  override val preHint = "Cage goblins protect nearby brutes from lighting."
  override val postHint = "A Flame Imp can survive the poison trap."
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
  val id = "LevelTiming1"
}
case object LevelPoisonHealing extends MapID {
  val id = "LevelPoisonHealing"
}
case object LevelArrowsAndDoors extends MapID {
  val id = "LevelArrowsAndDoors"
}
case object LevelManyArrows extends MapID {
  val id = "LevelManyArrows"
}
case object LevelVariarity extends MapID {
  val id = "LevelVariarity"
}
case object LevelFireArrow extends MapID {
  val id = "LevelFireArrow"
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

  val genericHintList = 
    Vector("Trolls have regeneration, so they can soak up a lot of damage.",
           "The Brute and Trap encyclopediae have detailed information and are accessible from the main menu",
           "The numbers by the stars tell you how many brutes have escaped and how many need to for the next star.",
           "The goblin shaman heals all nearby brutes, even Trolls",
           "A brute can only be healed by one goblin shaman at a time",
           "Reusable Trapdoors have a long recharge time between openings.")

  val randomPreHintList =
    genericHintList ++
    Vector(LevelArrow, LevelIntroBat, LevelEasy, LevelTrapdoor, LevelLightning).map(_.preHint)
  val randomPostHintList = 
    genericHintList ++
    Vector(LevelTrapdoor).map(_.postHint) ++
    Vector(LevelArrow, LevelIntroBat, LevelEasy, LevelTrapdoor, LevelLightning).map(_.preHint)

  implicit object Factory extends IDFactory[MapID] {
    val ids = Vector(LevelArrow, LevelIntroBat, LevelTrapdoor, LevelLightning, LevelEasy, LevelPoisonLightning, LevelManyArrows, LevelFireArrow, LevelPoisonHealing,LevelFire,LevelArrowsAndDoors,LevelGambit,LevelBullshit,LevelVariarity,LevelTiming)
  }
  implicit lazy val extractor =
    Json.extractor[String].map(x => if (Factory.fromString.isDefinedAt(x)) Factory.fromString(x) else Custom(x))
  implicit lazy val serializer =
    Json.serializer[String].contramap[MapID] { mid => mid.id }

  def fromInt(i: Int): MapID = Factory.ids(i-1)
}

case object maps extends IDMap[MapID, MapInfo]("data/maps.json")

