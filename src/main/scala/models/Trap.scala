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


class BaseTrap (val id: TrapID, val coord: Coordinate) {
  //grab stuff from IDMap
  val attr = TrapAttributeMap(id)

  //getThe right tile, and then try to damage each brute there
  def getInRangeBrutes: List[BaseBrute] = {
    List[BaseBrute]()
  }
  def attack() = {

  }

}

class TrapDoor(tCoord: Coordinate) extends BaseTrap(TrapDoorID, tCoord){
  
  var isOpen = false
  var isBlockedByWeb = false  
  //no damage, drop the brutes down to a lower level
  override def attack( ) = {

  }
}

class ReuseTrapDoor(tCoord: Coordinate) extends BaseTrap(ReuseTrapDoorID, tCoord) {
  var isOpen = false
  var isBlockedByWeb = false
  //no damage, drop the brutes down to a lower level, opens and closes using shotInterval
  override def attack() = {

  }
}

class Tar(tCoord: Coordinate) extends BaseTrap(TarID, tCoord) {
  override def attack() = {
    //probably apply a debuff on each enemy
  }
}

class Poison(tCoord: Coordinate) extends BaseTrap(PoisonID, tCoord) {
  override def attack() = {
    //either straight up deal damage or apply a debuff
  }
}

class Arrow(tCoord: Coordinate) extends BaseTrap(ArrowID, tCoord) {
  //if cur target is None, then get a target, then fire a projectile
  var curTarget: Option[BaseBrute] = None
  override def getInRangeBrutes = {
    //get a list of all brutes in the current floor (same y-value)
    List[BaseBrute]()
  }

  override def attack() = {
    //launch projectile
  }
}

class Lightning(tCoord: Coordinate) extends BaseTrap(LightningID, tCoord) {
  override def attack() = {
    //attack all in range
  }
}

class FlameVent(tCoord: Coordinate) extends BaseTrap(FlameVentID, tCoord) {
  override def attack() = {
    // attack all in range
  }
}

class HighBlade(tCoord: Coordinate) extends BaseTrap(HighBladeID, tCoord) {
  override def attack() = {

  }
}

object Trap {
  def apply(id: TrapID, coord:Coordinate) = {
    id match {
      case TrapDoorID => new TrapDoor(coord)
      case ReuseTrapDoorID => new ReuseTrapDoor(coord)
      case TarID => new Tar(coord)
      case PoisonID => new Poison(coord)
      case ArrowID => new Arrow(coord)
      case LightningID => new Lightning(coord)
      case FlameVentID => new FlameVent(coord)
      case HighBladeID => new HighBlade(coord)
      case _ => throw new Exception("Trap type not recognized")
    }
  }
}
