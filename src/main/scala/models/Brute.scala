package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory}

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class Coordinate(x: Float, y: Float)
case class BruteAttributes(
  maxHP: Int,
  moveSpeed: Float, //tiles/tick I guess
  width: Float,     //fraction of a tile
  height: Float,    //fraction of a tile
  flying: Boolean,  
  regen: Float,     //life regen per tick?
  coord: Coordinate,//location of the brute
  radius: Float     //radius of any aura abilities (heal/draw lightning)
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
    val ids = Vector(OgreID, GoblinID, VampireBatID)
    implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))
  }
}

case object BruteAttributeMap extends IDMap[BruteID, BruteAttributes]("data/brutes.json")


class BaseBrute (val id: BruteID) {
  //grab stuff from IDMap
  val attr = BruteAttributeMap(id) //= new BruteAttributes(10, 0.3, 0.5, 0.5, false, 1)
  def width = attr.width
  def height = attr.height
  var hp:Float  = attr.maxHP
  def hit(source: Unit, damage: Float) = {
    hp -= damage
  }
  def regenerate() = {
    hp += attr.regen
    hp = math.min(hp, attr.maxHP)
  }
  def isFlying = attr.flying
  var inAura: Set[BaseBrute] = Set[BaseBrute]()
  def getAuraEffectedBrutes() = {
    if (attr.radius == 0) {
      inAura.clear()
    }
  }
}

class Ogre extends BaseBrute(OgreID) {
}

class Goblin extends BaseBrute(GoblinID){
}

class VampireBat extends BaseBrute(VampireBatID){
}

class GoblinShaman extends BaseBrute(GoblinShamanID){
}

class Spider extends BaseBrute(SpiderID) {
}

class FlameImp extends BaseBrute(FlameImpID) {
  override def hit(source: Unit, damage: Float) = {
    //if source was fire take no damage, otherwise take full damage
    if (false) {
      hp -= damage
    }
  }
}

//factory for brutes
object Brute {
  def apply(id: BruteID) = {
    id match {
      case OgreID => new Ogre
      case _ => throw new Exception("1")
    }
  }
}