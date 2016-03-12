package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory}

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class TrapAttributes(
  damage: Float,   //damage of the trap
  shotInterval: Int, // number of ticks per shot
  targetFlying: Boolean // can target flying units
  )

trait TrapID
trait FloorTrapID extends TrapID
trait WallTrapID extends TrapID

case object TrapDoorID extends FloorTrapID
case object ReuseTrapDoorID extends FloorTrapID
case object TarID extends FloorTrapID

case object PoisonID extends WallTrapID
case object ArrowID extends WallTrapID
case object LightningID extends WallTrapID
case object FlameVentID extends WallTrapID
case object HighBladeID extends WallTrapID

object TrapID {
  implicit object Factory extends IDFactory[TrapID] {
    val ids = Vector(TrapDoorID, ReuseTrapDoorID, TarID, PoisonID, ArrowID, LightningID, FlameVentID, HighBladeID)
    implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))
  }
}

case object TrapAttributeMap extends IDMap[TrapID, TrapAttributes]("data/traps.json")


abstract class BaseTrap (val id: TrapID, val coord: Coordinate) {
  //grab stuff from IDMap
  val attr = TrapAttributeMap(id)
  
  var curTarget: Option[BaseBrute] = None
  def getTarget()

}
