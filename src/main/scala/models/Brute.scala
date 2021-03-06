package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory, TopLeftCoordinates}
import lib.util._

import rapture.json._
import rapture.json.jsonBackends.jackson._

case class Coordinate(var x: Float, var y:Float)

case class TimedEffect(val buffId: Option[BruteID], val debuffId: Option[TrapID], val n: Int) extends TimerListener {
  var active = true
  def isActive = active
  def deactivate() = active = false

  def tickOnce() = {
    if (ticking()) {
      tick(1)
    } else {
      cancelAll()
    }
  }

  this += new TickTimer(n, () => deactivate())
}
case class BruteAttributes(
  maxHP: Int,
  moveSpeed: Float, //tiles/tick I guess
  width: Float,     //fraction of a tile
  height: Float,    //fraction of a tile
  flying: Boolean,
  regen: Float,     //life regen per tick?
  radius: Float,    //radius of any aura abilities (heal/draw lightning)
  auraRegen: Float,  //Amount of regen of the aura
  description: String, // A brief description of the brute
  goldCost: Int
)

sealed trait BruteID {
  def imageList: List[Int]
  def image = imageList(0)
  def name : Int
}
case object OgreID extends BruteID {
  def imageList = List(R.drawable.ogre1, R.drawable.ogre2)
  def name = R.string.Ogre
}
case object GoblinID extends BruteID {
  def imageList = List(R.drawable.goblin1, R.drawable.goblin2)
  def name = R.string.Goblin
}
case object VampireBatID extends BruteID {
  def imageList = List(R.drawable.bat1, R.drawable.bat2)
  def name = R.string.VampireBat
}
case object GoblinShamanID extends BruteID {
  def imageList = List(R.drawable.goblinshaman1, R.drawable.goblinshaman2)
  def name = R.string.GoblinShaman
}
case object SpiderID extends BruteID {
  def imageList = List(R.drawable.spider1, R.drawable.spider2)
  def name = R.string.Spider
}
case object FlameImpID extends BruteID {
  def imageList = List(R.drawable.flame_imp1, R.drawable.flame_imp2)
  def name = R.string.FlameImp
}
case object CageGoblinID extends BruteID {
  def imageList = List(R.drawable.cage_goblin1, R.drawable.cage_goblin2)
  def name = R.string.CageGoblin
}
case object TrollID extends BruteID {
  def imageList = List(R.drawable.troll1, R.drawable.troll2)
  def name = R.string.Troll
}

object BruteID {
  implicit object Factory extends IDFactory[BruteID] {
    val ids = Vector(GoblinID, VampireBatID, FlameImpID, OgreID, GoblinShamanID, CageGoblinID, SpiderID, TrollID)
  }
  implicit lazy val extractor =
      Json.extractor[String].map(Factory.fromString(_))
}

case object BruteAttributeMap extends IDMap[BruteID, BruteAttributes]("data/brutes.json")


class BaseBrute (val id: BruteID, val coord: Coordinate) extends TopLeftCoordinates {
  //grab stuff from IDMap
  val attr = BruteAttributeMap(id)
  def width = attr.width
  def height = attr.height
  var hp: Float  = attr.maxHP
  def maxHP: Float  = attr.maxHP
  var isClimbingStairs = false
  var stairProgress = 0f
  var facingRight = false
  var currentFrame = 0
  var frameCounter = 0
  var tile : Tile = Game.map.getTile(Game.map.startTileCoord)

  var danceTicker: Boolean = false

  def isAlive = hp > 0

  def flyingHeight = 0f

  def hit(source: BaseTrap, damage: Float) : Unit = {
    //check source has lightning and effected by cage..
    if ( source.isInstanceOf[Lightning] && (effects.filter( x => {
                                              x.buffId match {
                                                case Some(id) => id == CageGoblinID && x.isActive
                                                case _ => false
                                              }}).size != 0)) {
      return
    }

    hp -= damage
    ()
  }

  def regenerate(): Unit = {
    if (! isAlive) return
    hp += attr.regen
    if (effects.filter(x => {
      x.buffId match {
        case Some(id) => id == GoblinShamanID && x.isActive
        case _ => false
      }}).size != 0) {
      hp += BruteAttributeMap(GoblinShamanID).auraRegen
    }
    if (effects.filter( x => {
      x.debuffId match {
        case Some(id) => id == PoisonID && x.isActive
        case _ => false
      }}).size != 0) {
      hp -= TrapAttributeMap(PoisonID).damage
    }
    hp = math.min(hp, attr.maxHP)
    ()
  }

  def isFlying = attr.flying
  def applyAura(brutes: List[BaseBrute]) = {
    val inRadius = brutes.filter(brute => distance(brute) <= attr.radius)
    inRadius.map(brute => brute.effects = TimedEffect(Some(id), None, Game.msAuraStickiness/Game.msPerTick)::brute.effects)
  }
  var effects = List[TimedEffect]()

  def incFrame(): Unit = {
    frameCounter = (frameCounter + 1) % 10
    if (frameCounter == 0) {
      currentFrame = (currentFrame + 1) % id.imageList.length
    }
  }

  def movingRight: Boolean = (this.bottomRightCoord._2 + 0.249f).toInt % 2 == 0

  def move(): Unit = {
    incFrame()
    // do some check on which floor you are on and decide whether to move left, right or climb ladder
    // tar slows speed

    if (isClimbingStairs) {
      val progressPerTick = attr.moveSpeed * 0.5f
      stairProgress += progressPerTick
      var climbingSpeed = attr.moveSpeed
      facingRight = true
      if ((movingRight && (stairProgress > 0.54)) ||
          !movingRight && (stairProgress < 0.54)) {
        climbingSpeed = -climbingSpeed
        facingRight = false
      }
      coord.x += climbingSpeed
      coord.y -= progressPerTick

      //done climbing stairs
      if (stairProgress >= 1) {
        facingRight = movingRight
        Game.game.map.getTile(if (movingRight) coord.copy(x = x + this.width) else coord).register(this)
        isClimbingStairs = false
        stairProgress = 0
        coord.y = (coord.y).toInt + 0.75f - height - flyingHeight
      }
      //if gameoption dancing is set do some flipping
      if (Game.Options.dancing && danceTicker) {
        facingRight = !facingRight
      }
      danceTicker = !danceTicker
      return
    }
    facingRight = movingRight
    var speed: Float = 0
    if (effects.filter( x => {
      x.debuffId match {
        case Some(id) => id == TarID && x.isActive
        case _ => false
      }}).size != 0) {
      speed = attr.moveSpeed * 0.5f
    } else {
      speed = attr.moveSpeed
    }
    //even levels move right, odd levels move left.
    var newX: Float = 0
    if (movingRight) {
      newX = coord.x + speed
    } else {
      newX = coord.x - speed
    }
    //check for map bounds
    if (newX < 1 && !movingRight) {
      isClimbingStairs = true
      stairProgress = 0
    }
    if ((newX + width) > Game.map.width - 1 && movingRight && coord.y.toInt != 0) {
      isClimbingStairs = true
      stairProgress = 0
    }
    //changed tiles, register/deregister
    val curX = if (movingRight) coord.x + this.width else coord.x
    val compareTo = if (movingRight) newX + this.width else newX
    if (curX.toInt != compareTo.toInt) {
      Game.game.map.getTile(coord.copy(x = compareTo)).register(this)
      coord.x = newX
    } else {
      coord.x = newX
    }
    ()
  }
  override def x = coord.x
  override def y = coord.y


  def cleanUpEffects() = {
    effects.filter(_.isActive)
  }

  def tickEffects () = {
    //ticks the effects
    effects.map(x => x.tickOnce())
  }

}

class Ogre(bCoord: Coordinate) extends BaseBrute(OgreID, bCoord)

class Goblin(bCoord: Coordinate) extends BaseBrute(GoblinID, bCoord)

class VampireBat(bCoord: Coordinate) extends BaseBrute(VampireBatID, bCoord){
  override def flyingHeight = 0.249f
  coord.y -= flyingHeight
  override def movingRight: Boolean = {
    //special case for stairs climbing
    if (x < 1 || x > Game.map.width - 1) {
      (this.bottomRightCoord._2 + 0.249f + flyingHeight).toInt % 2 == 0
    } else {
      (this.bottomRightCoord._2 + 0.249f).toInt % 2 == 0
    }
  }
}

class GoblinShaman(bCoord: Coordinate) extends BaseBrute(GoblinShamanID, bCoord)

class Spider(bCoord: Coordinate) extends BaseBrute(SpiderID, bCoord)

class FlameImp(bCoord: Coordinate) extends BaseBrute(FlameImpID, bCoord) {
  override def hit(source: BaseTrap, damage: Float) = {
    //if source was fire take no damage, otherwise do normal damage calculation
    if (!source.isInstanceOf[FlameVent]) {
      super.hit(source, damage)
    }
  }
}

class CageGoblin(bCoord: Coordinate) extends BaseBrute(CageGoblinID, bCoord)

class Troll(bCoord: Coordinate) extends BaseBrute(TrollID, bCoord)

//factory for brutes
object Brute {
  def apply(id: BruteID, coord: Coordinate): BaseBrute = {
    val brute = id match {
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
    brute.coord.y += (1-brute.height - 1/4f)
    brute.coord.x += (1-brute.width)
    brute
  }
}
