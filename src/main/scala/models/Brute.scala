package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory, TopLeftCoordinates}

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class Coordinate(var x: Float, var y:Float)

case class BruteAttributes(
  maxHP: Int,
  moveSpeed: Float, //tiles/tick I guess
  width: Float,     //fraction of a tile
  height: Float,    //fraction of a tile
  flying: Boolean,
  regen: Float,     //life regen per tick?
  radius: Float,    //radius of any aura abilities (heal/draw lightning)
  auraRegen: Float,  //Amount of regen of the aura
  description: String // A brief description of the brute
  )

sealed trait BruteID {
  def image: Int
}
case object OgreID extends BruteID {
  def image = R.drawable.ogre1
}
case object GoblinID extends BruteID {
  def image = R.drawable.goblin1
}
case object VampireBatID extends BruteID {
  def image = R.drawable.bat
}
case object GoblinShamanID extends BruteID {
  def image = R.drawable.goblinshaman1
}
case object SpiderID extends BruteID {
  def image = R.drawable.ogre1
}
case object FlameImpID extends BruteID {
  def image = R.drawable.ogre1
}
case object CageGoblinID extends BruteID {
  def image = R.drawable.ogre1
}
case object TrollID extends BruteID {
  def image = R.drawable.ogre1
}

object BruteID {
  implicit object Factory extends IDFactory[BruteID] {
    val ids = Vector(OgreID, GoblinID, VampireBatID, GoblinShamanID, SpiderID, FlameImpID, CageGoblinID, TrollID)
    implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))
  }
}

case object BruteAttributeMap extends IDMap[BruteID, BruteAttributes]("data/brutes.json")


class BaseBrute (val id: BruteID, val coord: Coordinate) extends TopLeftCoordinates {
  //grab stuff from IDMap
  val attr = BruteAttributeMap(id) //= new BruteAttributes(10, 0.3, 0.5, 0.5, false, 1)
  def width = attr.width
  def height = attr.height
  var hp: Float  = attr.maxHP
  var isClimbingStairs = false
  var stairProgress = 0f

  def isAlive = hp > 0

  def hit(source: BaseTrap, damage: Float) : Unit = {
    //check source has lightning and effected by cage..
    if ( source.isInstanceOf[Lightning] && (buffs contains CageGoblinID)) {
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
    val inRadius = brutes.filter(brute => distance(brute) <= attr.radius)
    inRadius.map(brute => brute.buffs += id)
  }
  var buffs = Set[BruteID]()
  var debuffs = Set[TrapID]()



  def move(map: GameMap): Unit = {
    //probably do some check on which floor you are on and decide whether to move left, right or climb ladder
    //tar slows speed
    if (isClimbingStairs) {
      val progressPerTick = 0.02f
      stairProgress += progressPerTick
      //done climbing stairs
      if (stairProgress > 1) {
        map.getTile(coord).deregister(this)
        coord.y = coord.y + 1
        map.getTile(coord).register(this)
        isClimbingStairs = false
        stairProgress = 0
      }
      return
    }
    var speed: Float = 0
    if (debuffs contains TarID) {
      speed = attr.moveSpeed * 0.5f
    } else {
      speed = attr.moveSpeed
    }

    //even levels move left, odd levels move right.
    var newX: Float = 0
    if (y%2 == 0) {
      newX = coord.x - speed
    } else {
      newX = coord.x + speed
    }
    //check for map bounds
    if (newX < 0) {
      newX = 0
      isClimbingStairs = true
      stairProgress = 0
    }
    val sizeOfMap = 6
    if (newX > sizeOfMap) {
      newX = sizeOfMap
      isClimbingStairs = true
      stairProgress = 0
    }
    //changed tiles, register/deregister
    if (coord.x.toInt != newX.toInt) {
      map.getTile(coord).deregister(this)
      coord.x = newX
      map.getTile(coord).register(this)
    } else {
      coord.x = newX
    }
    ()
  }
  override def x = coord.x
  override def y = coord.y

  //clear buffs every ~ 1/2 second then reapply them
  def clearBuffs() = {
    buffs.clear
    debuffs.clear
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
  override def hit(source: BaseTrap, damage: Float) = {
    //if source was fire take no damage, otherwise do normal damage calculation
    if (!source.isInstanceOf[FlameVent]) {
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
  def apply(id: BruteID, coord: Coordinate): BaseBrute = {
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
