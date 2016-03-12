package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory}

import rapture.json._
import rapture.json.jsonBackends.jackson._


case class Coordinate(var x: Float, var y: Float)
case class BruteAttributes(
  maxHP: Int,
  moveSpeed: Float, //tiles/tick I guess
  width: Float,     //fraction of a tile
  height: Float,    //fraction of a tile
  flying: Boolean,  
  regen: Float,     //life regen per tick?
  radius: Float,    //radius of any aura abilities (heal/draw lightning)
  auraRegen: Float  //Amount of regen of the aura
  )

trait BruteID
case object OgreID extends BruteID
case object GoblinID extends BruteID
case object VampireBatID extends BruteID
case object GoblinShamanID extends BruteID
case object SpiderID extends BruteID
case object FlameImpID extends BruteID
case object CageGoblinID extends BruteID
case object TrollID extends BruteID

object BruteID {
  implicit object Factory extends IDFactory[BruteID] {
    val ids = Vector(OgreID, GoblinID, VampireBatID, GoblinShamanID, SpiderID, FlameImpID, CageGoblinID, TrollID)
    implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))
  }
}

case object BruteAttributeMap extends IDMap[BruteID, BruteAttributes]("data/brutes.json")


class BaseBrute (val id: BruteID, val coord: Coordinate) {
  //grab stuff from IDMap
  val attr = BruteAttributeMap(id) //= new BruteAttributes(10, 0.3, 0.5, 0.5, false, 1)
  def width = attr.width
  def height = attr.height
  var hp:Float  = attr.maxHP

  def isAlive = hp > 0
  
  def hit(source: Unit, damage: Float) : Unit= {
    //check source has lightning and effected by cage..
    if ( false && (buffs contains CageGoblinID)) {
      return
    }
    //check if source can hit flying
    if (false) {
      return
    }
    hp -= damage
    ()
  }

  def regenerate(): Unit = {
    if (! isAlive) return
    hp += attr.regen
    if (buffs contains GoblinShamanID) {
      hp += BruteAttributeMap(GoblinShamanID).auraRegen
    }
    hp = math.min(hp, attr.maxHP)
    ()
  }
  
  def isFlying = attr.flying
  def applyAura(brutes: List[BaseBrute]) = {
    val inRadius = brutes.filter(brute => distance(brute) <= attr.radius * attr.radius)
    inRadius.map(brute => brute.buffs += id)
  }
  var buffs = Set[BruteID]()
  
  def distance(otherBrute: BaseBrute): Float = {
    val dx = otherBrute.coord.x - coord.x
    val dy = otherBrute.coord.y - coord.y
    dx*dx + dy*dy
  }

  def move() = {
    coord.x = coord.x + attr.moveSpeed
  }
}

class Ogre(bCoord: Coordinate) extends BaseBrute(OgreID, bCoord) {
}

class Goblin(bCoord: Coordinate) extends BaseBrute(GoblinID, bCoord){
}

class VampireBat(bCoord: Coordinate) extends BaseBrute(VampireBatID, bCoord){
}

class GoblinShaman(bCoord: Coordinate) extends BaseBrute(GoblinShamanID, bCoord){
}

class Spider(bCoord: Coordinate) extends BaseBrute(SpiderID, bCoord) {
}

class FlameImp(bCoord: Coordinate) extends BaseBrute(FlameImpID, bCoord) {
  override def hit(source: Unit, damage: Float) = {
    //if source was fire take no damage, otherwise do normal damage calculation
    if (false) {
      super.hit(source, damage)
    }
  }
}

class CageGoblin(bCoord: Coordinate) extends BaseBrute(CageGoblinID, bCoord) {
}

class Troll(bCoord: Coordinate) extends BaseBrute(TrollID, bCoord){
}

//factory for brutes
object Brute {
  def apply(id: BruteID, coord: Coordinate) = {
    id match {
      case OgreID => new Ogre(coord)
      case GoblinID => new Goblin(coord)
      case VampireBatID => new VampireBat(coord)
      case GoblinShamanID => new GoblinShaman(coord)
      case SpiderID => new Spider(coord)
      case FlameImpID => new FlameImp(coord)
      case CageGoblinID => new CageGoblin(coord)
      case TrollID => new Troll(coord)
      case _ => throw new Exception("Unrecognized Brute Type")
    }
  }
}