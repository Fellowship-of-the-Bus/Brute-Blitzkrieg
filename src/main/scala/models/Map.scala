package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set

import lib.game.{IDMap, IDFactory}

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class Tile(val floorTrapID: Int, val wallTrapID: Int) {
	val bruteList: Set[Brute] = Set[Brute]()

	def register(e: Brute) = {
    bruteList += e
  }

  def deregister(e: Brute) = {
    bruteList -= e
  }
}


case class MapInfo(
  tiles: List[List[Tile]],
  startTileCoord: Int, //(Int, Int),
  endTileCoord: Int) //(Int, Int))

class Map(num : Int) {
  val brute = new Brute()
}

sealed trait MapID
case object Level1 extends MapID

object MapID {
  implicit object Factory extends IDFactory[MapID] {
    val ids = Vector(Level1)
    implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))
  }
}

case object maps extends IDMap[MapID, MapInfo]("data/maps.json")
