package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory, TopLeftCoordinates}
import lib.util._

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class TrapAttributes(
  damage: Float,   //damage of the trap
  shotInterval: Int, // number of ticks per shot
  targetFlying: Boolean, // can target flying units
  description: String
  )

sealed trait TrapID {
  def image: Int
  def name: Int
}
trait FloorTrapID extends TrapID
trait WallTrapID extends TrapID

case object TrapDoorID extends FloorTrapID {
  def image = R.drawable.ahmed2
  def name = R.string.Trapdoor
}
case object ReuseTrapDoorID extends FloorTrapID {
  def image = R.drawable.ahmed2
  def name = R.string.ReusableTrapdoor
}
case object TarID extends FloorTrapID {
  def image = R.drawable.tar
  def name = R.string.Tar
}

case object PoisonID extends WallTrapID {
  def image = R.drawable.poison_vent
  def name = R.string.Poison
}
case object ArrowID extends WallTrapID {
  def image = R.drawable.arrowtrap
  def name = R.string.Arrow
}
case object LightningID extends WallTrapID {
  def image = R.drawable.lightningtrap
  def name = R.string.Lightning
}
case object FlameVentID extends WallTrapID {
  def image = R.drawable.flame_vent
  def name = R.string.FlameVent
}
case object HighBladeID extends WallTrapID {
  def image = R.drawable.high_blade1
  def name = R.string.HighBlade
}

case object NoTrapID extends TrapID {
  def image = 0
  def name = 0
}

object TrapID {
  implicit object Factory extends IDFactory[TrapID] {
    val ids = Vector(TrapDoorID, ReuseTrapDoorID, TarID, PoisonID, ArrowID, LightningID, FlameVentID, HighBladeID)
  }
  implicit lazy val extractor = Json.extractor[String].map(Factory.fromString(_))

}

case object TrapAttributeMap extends IDMap[TrapID, TrapAttributes]("data/traps.json")


class BaseTrap (val id: TrapID, val coord: Coordinate) extends TopLeftCoordinates with TimerListener{
  //grab stuff from IDMap
  val attr = TrapAttributeMap(id)

  var canAttack = true
  //getThe right tile, and then try to damage each brute there
  def getInRangeBrutes: List[BaseBrute] = {
    Game.game.map.getTile(coord).bruteList.toList
    //List[BaseBrute]()
  }
  def attack(): Option[BaseProjectile] = {
    tickOnce()
    //get brutes in range
    val listOfBrutes = getInRangeBrutes
    if (canAttack && listOfBrutes.length != 0) {

      //for each in range, attack
      listOfBrutes.map(brute => {
          // don't attack if target is flying but trap cannot target flying things
          if (attr.targetFlying || !brute.attr.flying) brute.hit(this, attr.damage)
        })
      setCooldown()
    }
    None
  }
  def setCooldown() = {
    canAttack = false
    add(new TickTimer(attr.shotInterval, () => canAttack = true))
  }
  def tickOnce() = {
    if (ticking()) {
      tick(1)
    } else {
      cancelAll()
    }
  }

  override def x = coord.x
  override def y = coord.y

  override def height = 1
  override def width = 1

}

class TrapDoor(tCoord: Coordinate) extends BaseTrap(TrapDoorID, tCoord){

  var isOpen = false
  var isBlockedByWeb = false
  canAttack = true
  //no damage, drop the brutes down to a lower level, check if spider is over the trap, if so block
  override def attack(): Option[BaseProjectile] = {
    if (isBlockedByWeb) {
      return None
    } else {
      val listOfBrutes = getInRangeBrutes
      //check if any is in range. If so, open the trap
      if (listOfBrutes.length != 0) {
        if (isOpen == false) {
          isOpen = true
        }
      } else {
        return None
      }
      if(listOfBrutes.filter(brute => brute.id == SpiderID).length >= 1) {
        isBlockedByWeb = true
      } else {
        listOfBrutes.map(brute => brute.coord.y += 1)
        //merge brute sets from our tile into the tile below us

        val curTile = Game.game.map.getTile(coord)
        val tileBelow = Game.game.map.getTile(Coordinate(coord.x, coord.y+1))
        tileBelow.bruteList ++= curTile.bruteList
        curTile.bruteList.clear
      }
    }
    None
  }
}

class ReuseTrapDoor(tCoord: Coordinate) extends BaseTrap(ReuseTrapDoorID, tCoord) {
  var isOpen = false
  var isBlockedByWeb = false
  //no damage, drop the brutes down to a lower level, opens and closes using shotInterval
  override def attack(): Option[BaseProjectile] = {
    //check if we can attack
    tickOnce()
    if (!canAttack && !isOpen) {
      return None
    }
    if (isBlockedByWeb) {
      return None
    } else {
      val listOfBrutes = getInRangeBrutes
      if (listOfBrutes.length == 0){
        return None
      }
      isOpen = true
      // add a timer to close the trap after some number of ticks
      add(new TickTimer(10, () => isOpen = false))
      //check for spider
      if(listOfBrutes.filter(brute => brute.id == SpiderID).length >= 1) {
        isBlockedByWeb = true
      } else {
        listOfBrutes.map(brute => brute.coord.y += 1)
        //merge brute sets from our tile into the tile below us

        val curTile = Game.game.map.getTile(coord)
        val tileBelow = Game.game.map.getTile(Coordinate(coord.x, coord.y+1))
        tileBelow.bruteList ++= curTile.bruteList
        curTile.bruteList.clear
      }
      //set the cooldown timer if we havent yet
      if (canAttack) setCooldown()
    }
    None
  }
}

class Tar(tCoord: Coordinate) extends BaseTrap(TarID, tCoord) {
  override def attack(): Option[BaseProjectile] = {
    tickOnce()
    //probably apply a debuff on each
    val listOfBrutes = getInRangeBrutes
    if (canAttack && listOfBrutes.length != 0) {

      //for each in range, attack
      listOfBrutes.map(brute => brute.debuffs += this.id)
      setCooldown()
    }
    None
  }
}

class Poison(tCoord: Coordinate) extends BaseTrap(PoisonID, tCoord) {
  // override def attack(): Option[BaseProjectile] = {
  //   //either straight up deal damage or apply a debuff
  //   None
  // }
}

class Arrow(tCoord: Coordinate) extends BaseTrap(ArrowID, tCoord) {
  //if cur target is None, then get a target, then fire a projectile
  var curTarget: Option[BaseBrute] = None
  override def getInRangeBrutes: List[BaseBrute] = {
    //get a list of all brutes in the current floor (same y-value)
    val setOfBrutes: Set[BaseBrute] = Set[BaseBrute]()
    Game.game.map.tiles(y.toInt).map(tile => setOfBrutes ++= tile.bruteList)
    setOfBrutes.toList
  }

  override def attack() : Option[BaseProjectile]= {
    tickOnce()
    if (!canAttack) return None
    curTarget match {
      case Some(brute) => {
        val dy = y.toInt - brute.y.toInt
        //check not climbing stairs and on same floor
        if (!brute.isClimbingStairs && dy < 0.5) {
          setCooldown()
          return Some(new ArrowProjectile(ArrowProj, coord, attr.damage, this, brute))
        }
      }
      case _ => ()
    }
    val target = getNewTarget()
    target match {
      case Some(brute) => {
        curTarget = Some(brute)
        setCooldown()
        return Some(new ArrowProjectile(ArrowProj, coord, attr.damage, this, brute))
      }
      case None => None
    }
  }

  def getNewTarget(): Option[BaseBrute] = {
    val listOfBrutes = getInRangeBrutes
      if (listOfBrutes.length == 0) {
        return None
      } else {
        // some targeting heuristic
        None
      }
  }
}

class Lightning(tCoord: Coordinate) extends BaseTrap(LightningID, tCoord) {
  // override def attack(): Option[BaseProjectile] = {
  //   //attack all in range
  //   None
  // }
}

class FlameVent(tCoord: Coordinate) extends BaseTrap(FlameVentID, tCoord) {
  // override def attack(): Option[BaseProjectile] = {
  //   // attack all in range
  //   None
  // }
}

class HighBlade(tCoord: Coordinate) extends BaseTrap(HighBladeID, tCoord) {
  // override def attack(): Option[BaseProjectile] = {
  //   None
  // }
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
