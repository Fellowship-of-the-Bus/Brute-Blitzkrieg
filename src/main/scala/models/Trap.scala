package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set

import org.scaloid.common._

import lib.game.{IDMap, IDFactory, TopLeftCoordinates}
import lib.util._

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class TrapAttributes(
  damage: Float,         //damage of the trap
  duration: Int,         // Number of ticks attack persists
  shotInterval: Int,     // number of ticks per shot
  targetFlying: Boolean, // can target flying units
  description: String
)

sealed trait TrapID {
  def image: Int
  def name: Int
  def string = Game.res.getString(name)
}
trait FloorTrapID extends TrapID
trait WallTrapID extends TrapID

case object TrapdoorID extends FloorTrapID {
  def image = R.drawable.trapdoor_closed
  def name = R.string.Trapdoor
}
case object TrapdoorOpenID extends FloorTrapID {
  def image = R.drawable.trapdoor_open
  def name = R.string.Trapdoor
}
case object TrapdoorWebbedID extends FloorTrapID {
  def image = R.drawable.trapdoor_webbed
  def name = R.string.Trapdoor
}
case object ReuseTrapdoorID extends FloorTrapID {
  def image = R.drawable.reusable_trapdoor
  def name = R.string.ReusableTrapdoor
}
case object ReuseTrapdoorOpenID extends FloorTrapID {
  def image = R.drawable.trapdoor_open
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
case object LeftArrowID extends WallTrapID {
  def image = R.drawable.arrowtrap
  def name = R.string.LeftArrow
}
case object ArrowID extends WallTrapID {
  def image = R.drawable.rightarrowtrap
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
case object HighBlade2ID extends WallTrapID {
  def image = R.drawable.high_blade2
  def name = R.string.HighBlade
}

case object NoTrapID extends TrapID {
  def image = 0
  def name = 0
  override def string = "NoTrap"
}

object TrapID {
  implicit object Factory extends IDFactory[TrapID] {
    val ids = Vector(TrapdoorID, ReuseTrapdoorID, TarID, LeftArrowID, ArrowID, PoisonID, FlameVentID, LightningID, HighBladeID)
    val openIds = Vector(TrapdoorOpenID, ReuseTrapdoorOpenID, TrapdoorWebbedID, HighBlade2ID)
  }
  implicit lazy val extractor = Json.extractor[String].map(x => if (x == "NoTrapID") NoTrapID else Factory.fromString(x))
  implicit lazy val serializer = Json.serializer[String].contramap[TrapID](x => x.string.replace(" ", "") + "ID")
}

case object TrapAttributeMap extends IDMap[TrapID, TrapAttributes]("data/traps.json")


class BaseTrap (var id: TrapID, val coord: Coordinate) extends TopLeftCoordinates with TimerListener{
  //grab stuff from IDMap
  val attr = TrapAttributeMap(id)

  var canAttack = true
  //getThe right tile, and then try to damage each brute there
  def getInRangeBrutes: List[BaseBrute] = {
    Game.game.map.getTile(coord).bruteList.toList.filter(_.isAlive)
    //List[BaseBrute]()
  }
  def attack(): List[BaseProjectile] = {
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
      fireProjectiles(listOfBrutes)
    } else {
      List[BaseProjectile]()
    }
  }
  def setCooldown() = {
    canAttack = false
    this += new TickTimer(attr.shotInterval, () => canAttack = true)
  }
  def tickOnce() = {
    if (ticking()) {
      tick(1)
    } else {
      cancelAll()
    }
  }

  def fireProjectiles(targets: List[BaseBrute]) : List[BaseProjectile] = {
    var projList: List[BaseProjectile] = List[BaseProjectile]()
    val proj = fireProjectile(targets.head)
    proj match {
      case Some(projectile) => projList = projectile::projList
      case None => ()
    }
    projList
  }
  def fireProjectile(target: BaseBrute) : Option[BaseProjectile] = None

  override def x = coord.x
  override def y = coord.y

  override def height = 1
  override def width = 1

}
class FloorTrap(tid: FloorTrapID, tCoord: Coordinate) extends BaseTrap(tid, tCoord) {
  override def height = 1/4f
  override def width = 1
}

class WallTrap(tid: WallTrapID, tCoord: Coordinate) extends BaseTrap(tid, tCoord) {
  override def height = 3/4f
  override def width = 1
}

class Trapdoor(tid: FloorTrapID, tCoord: Coordinate) extends FloorTrap(tid, tCoord){
  override def height = 3/4f

  var isOpen = false
  var isBlockedByWeb = false
  canAttack = true
  val altid : FloorTrapID = TrapdoorOpenID
  val belowCoord = new Coordinate(tCoord.x, tCoord.y + 1)
  //no damage, drop the brutes down to a lower level, check if spider is over the trap, if so block
  override def attack(): List[BaseProjectile] = {
    if (isBlockedByWeb) {
      return List[BaseProjectile]()
    } else {
      val listOfBrutes = getInRangeBrutes
      val belowBrutes = Game.game.map.getTile(belowCoord).bruteList.toList.filter(_.isAlive)
      //check if any is in range. If so, open the trap
      if (listOfBrutes.filter(!_.attr.flying).length != 0) {
        if (isOpen == false) {
          isOpen = true
          id = altid
        }
      }

      if (isOpen) {
        if(listOfBrutes.filter(brute => brute.id == SpiderID).length >= 1) {
          isBlockedByWeb = true
          id = TrapdoorWebbedID
        } else {
          listOfBrutes.map(brute => {
            if (!brute.attr.flying) {
              Game.game.map.getTile(brute.coord).deregister(brute)
              brute.coord.y += 1
              brute.facingRight = !brute.facingRight
              Game.game.map.getTile(brute.coord).register(brute)
            }

          })
          belowBrutes.map(brute => {
            if (brute.attr.flying) {
              Game.game.map.getTile(brute.coord).deregister(brute)
              brute.coord.y -= 1
              brute.facingRight = !brute.facingRight
              Game.game.map.getTile(brute.coord).register(brute)
            }
          })
        }
      }
    }
    List[BaseProjectile]()
  }

  override def getInRangeBrutes: List[BaseBrute] = {
    Game.game.map.getTile(coord).bruteList.toList.filter(b => {
      val progress = b.x % 1
      if (b.facingRight) {
        (progress - b.width) > 0
      } else {
        (1 - progress) > b.width
      }
    })
  }
}

class ReuseTrapdoor(tCoord: Coordinate) extends Trapdoor(ReuseTrapdoorID, tCoord) {
  override def height = 3/4f

  //var isOpen = false
  //var isBlockedByWeb = false
  override val altid = ReuseTrapdoorOpenID
  //no damage, drop the brutes down to a lower level, opens and closes using shotInterval
  override def attack(): List[BaseProjectile] = {
    tickOnce()
    if (isOpen || canAttack) {
      val wasOpen = isOpen
      super.attack()
      if (!wasOpen && isOpen) {
        this += new TickTimer(10, () => {
            isOpen = false
            if (!isBlockedByWeb)
              id = ReuseTrapdoorID
          })
        setCooldown()
      }
    }
    List[BaseProjectile]()
  }

  override def getInRangeBrutes: List[BaseBrute] = {
    Game.game.map.getTile(coord).bruteList.toList.filter(b => {
      val progress = b.x - b.x.toInt
      if (b.facingRight) {
        true
      } else {
        1 - progress > b.width
      }
    })
  }
}

class Tar(tCoord: Coordinate) extends FloorTrap(TarID, tCoord) {
  override def attack(): List[BaseProjectile] = {
    tickOnce()
    //probably apply a debuff on each
    val listOfBrutes = getInRangeBrutes
    if (canAttack && listOfBrutes.length != 0) {

      //for each in range, attack
      listOfBrutes.filter(b => !b.isFlying).map(brute => brute.effects = TimedEffect(None, Some(id), Game.msAuraStickiness/Game.msPerTick)::brute.effects)
      setCooldown()
    }
    List[BaseProjectile]()
  }
}

class Poison(tCoord: Coordinate) extends WallTrap(PoisonID, tCoord) {
  override def attack(): List[BaseProjectile] = {
    tickOnce()
    //get brutes in range
    val listOfBrutes = getInRangeBrutes
    if (canAttack && listOfBrutes.length != 0) {

      //for each in range, give them the poison debuff
      listOfBrutes.map(brute => {
          brute.effects = TimedEffect(None, Some(id), (2f*Game.msAuraStickiness/Game.msPerTick).toInt)::brute.effects
        })
      setCooldown()
      new PoisonProjectile(PoisonProj, coord.copy(), this, null)::List[BaseProjectile]()
    } else {
      List[BaseProjectile]()
    }
  }
}

class Arrow(tid: WallTrapID, tCoord: Coordinate) extends WallTrap(tid, tCoord) {
  //if cur target is None, then get a target, then fire a projectile
  var curTarget: Option[BaseBrute] = None
  override def getInRangeBrutes: List[BaseBrute] = {
    //get a list of all brutes in the current floor (same y-value)
    val setOfBrutes: Set[BaseBrute] = Set[BaseBrute]()
    Game.game.map.tiles(y.toInt).map(tile => setOfBrutes ++= tile.bruteList)
    setOfBrutes.toList.filter( x => x.isAlive && ! x.isClimbingStairs)
  }

  override def attack() : List[BaseProjectile]= {
    tickOnce()

    if (!canAttack) return List[BaseProjectile]()
    curTarget match {
      case Some(brute) => {
        val dy = y.toInt - brute.y.toInt
        //check not climbing stairs and on same floor
        if (brute.isClimbingStairs || dy != 0 || !brute.isAlive) {
          curTarget = getNewTarget()
        }
      }
      case None => {
        curTarget = getNewTarget()
      }
    }

    curTarget match {
      case Some(brute) => {
        setCooldown()
        return List[BaseProjectile](new ArrowProjectile(ArrowProj, coord.copy(), attr.damage, this, brute))
      }
      case None => List[BaseProjectile]()
    }
  }

  def getNewTarget(): Option[BaseBrute] = {
    val listOfBrutes = getInRangeBrutes
    if (listOfBrutes.length == 0) {
      return None
    } else {
      // Closest to end
      if (coord.y % 2  == 0) {
        return Some(listOfBrutes.maxBy(_.x))
      } else {
        return Some(listOfBrutes.minBy(_.x))
      }
    }
  }
}

class LeftArrow(tCoord: Coordinate) extends Arrow(LeftArrowID, tCoord) {
  override def getInRangeBrutes: List[BaseBrute] = {
    val listOfBrutes = super.getInRangeBrutes
    listOfBrutes.filter( b => b.x < (coord.x + 0.5f))
  }
}

class RightArrow(tCoord: Coordinate) extends Arrow(ArrowID, tCoord) {
  override def getInRangeBrutes: List[BaseBrute] = {
    val listOfBrutes = super.getInRangeBrutes
    listOfBrutes.filter( b => b.x > (coord.x + 0.5f))
  }
}

class Lightning(tCoord: Coordinate) extends WallTrap(LightningID, tCoord) {
  override def height = 3/8f

  def firingCoord(towerCoord: Coordinate) = {
    val c = coord.copy()
    c.x += width / 2
    c.y += height / 2
    c
  }

  override def getInRangeBrutes: List[BaseBrute] = {
    val leftCoord = coord.copy()
    leftCoord.x = leftCoord.x - 1
    val rightCoord = coord.copy()
    rightCoord.x = rightCoord.x + 1

    (Game.game.map.getTile(coord).bruteList.toList.filter(_.isAlive)
    :::Game.game.map.getTile(leftCoord).bruteList.toList.filter(_.isAlive)
    :::Game.game.map.getTile(rightCoord).bruteList.toList.filter(_.isAlive))
  }

  override def fireProjectiles(targets: List[BaseBrute]) : List[BaseProjectile] = {
    var projList: List[BaseProjectile] = List[BaseProjectile]()
    for (brute <- targets) {
      val proj = fireProjectile(brute)
      proj match {
        case Some(projectile) => projList = projectile::projList
        case None => ()
      }
    }
    projList
  }

  override def fireProjectile(brute: BaseBrute): Option[BaseProjectile] = {
    Some(new LightningProjectile(LightningProj, firingCoord(coord), this, brute))
  }
}

class FlameVent(tCoord: Coordinate) extends WallTrap(FlameVentID, tCoord) {
  var attacking = false
  override def setCooldown () = {
    if (!attacking) {
      attacking = true
      this += new TickTimer(attr.duration, () => {
                                      canAttack = false
                                      attacking = false})
      this += new TickTimer(attr.shotInterval, () => canAttack = true)
    }
  }
  override def fireProjectile(brute: BaseBrute): Option[BaseProjectile] = {
    Some(new FireProjectile(FireProj, coord.copy(), this, brute))
  }
}

class HighBlade(tCoord: Coordinate) extends WallTrap(HighBladeID, tCoord) {
  override def height = 3/8f
  var imgSwapCounter = 0
  val altid : WallTrapID = HighBlade2ID
  override def getInRangeBrutes: List[BaseBrute] = {
    Game.game.map.getTile(coord).bruteList.toList.filter(b => {
      b.height >= 0.6f || b.isFlying
    })
  }
  override def attack(): List[BaseProjectile] = {
    imgSwapCounter = (imgSwapCounter + 1) % 4
    if (imgSwapCounter == 0) {
      if (id == altid) {
        id = HighBladeID
      } else {
        id = altid
      }
    }
    super.attack();
  }
}

object Trap {
  def apply(id: TrapID, coord:Coordinate) = {
    id match {
      case TrapdoorID => new Trapdoor(TrapdoorID, coord)
      case ReuseTrapdoorID => new ReuseTrapdoor(coord)
      case TarID => new Tar(coord)
      case PoisonID => new Poison(coord)
      case LeftArrowID => new LeftArrow(coord)
      case ArrowID => new RightArrow(coord)
      case LightningID => new Lightning(coord)
      case FlameVentID => new FlameVent(coord)
      case HighBladeID => new HighBlade(coord)
      case _ => throw new Exception("Trap type not recognized")
    }
  }
}
