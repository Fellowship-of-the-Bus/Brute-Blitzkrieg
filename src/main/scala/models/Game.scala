package com.github.fellowship_of_the_bus
package bruteb
package models


import java.util.{Timer, TimerTask}

object Game {
  var game: Game = null
}

class Game(val map: MapInfo, var brutes: Vector[BruteID] = Vector[BruteID](null, null, null, null)) {
  val ticksPerSecond = 20

  val msPerTick = 1000/ticksPerSecond          //20 ticks/sec
  val msPerCleanup = msPerTick*40     //cleanup every 2 secs
  val msAuraStickiness = msPerTick*10  //update auras every 10 ticks
  var bruteList: List[BaseBrute] = List[BaseBrute]()
  var trapList: List[BaseTrap] = List[BaseTrap]()
  var projList: List[BaseProjectile] = List[BaseProjectile]()

  var battleCanvas: BattleCanvas = null
  var timer: Timer = null//new Timer()

  var currentGold = map.startingGold

  //make the towers that the map needs
  for (y <- map.height - 1 to 0 by -1) {
    for (x <- 0 until map.width ) {
      val tile = map.tiles(y)(x)
      addTrapFromID(tile.floorTrapID, Coordinate(x,y+3/4f))
      addTrapFromID(tile.wallTrapID, Coordinate(x,y))
    }
  }

  def addTrapFromID(id: TrapID, coord:Coordinate) = {
    if (id != NoTrapID) {
      val trap = Trap(id, coord)
      trapList = trap::trapList
    }
  }

  //function for sending brutes
  def sendBrute(id: BruteID) = {
    //check that we have enough gold
    if (currentGold >= BruteAttributeMap(id).goldCost) {
      val brute = Brute(id, new Coordinate(map.startTileCoord.x, map.startTileCoord.y))
      bruteList = brute::bruteList
      currentGold = currentGold - BruteAttributeMap(id).goldCost
    }
  }

  def tick() = {
    //order of actions:
    //move brutes
    //check for brutes at exit
    //regenerate brutes
    //expire effects
    //move projectiles
    //fire towers
    for (brute <- bruteList.filter(_.isAlive)) {
      brute.move()
      //if brute reached end
      if (map.getTile(brute.coord) eq map.getTile(map.endTileCoord)) {
        //just kill for now do something more sophiscated later
        brute.hp = -1
      }
      brute.regenerate()
      brute.tickEffects()
    }
    for (proj <- projList.filter(_.isActive)) {
      proj.move()
    }
    for (trap <- trapList) {
      val proj = trap.attack()
      proj match {
        case Some(projectile) => projList = projectile::projList
        case None => ()
      }
    }
    //battleCanvas.postInvalidate()
  }

  def cleanup() = {
    //actions: clean up inactive projectiles, dead brutes
    projList = projList.filter(_.isActive)
    bruteList = bruteList.filter(_.isAlive)
  }

  def updateAuras() = {
    //clear auras, then apply them
    for (brute <- bruteList.filter(_.isAlive)) {
      brute.cleanUpEffects()
    }
    for (brute <- bruteList.filter(_.isAlive)) {
      brute.applyAura(bruteList.filter(_.isAlive))
    }
  }

  //start timers also call for resume
  def startGame () = {
    timer = new Timer()
    timer.scheduleAtFixedRate(new TimerTask() {
      override def run() = {
        tick()
      }
    }, 0, msPerTick)
    timer.scheduleAtFixedRate(new TimerTask() {
      override def run() = {
        cleanup()
      }
    }, 0, msPerCleanup)
    timer.scheduleAtFixedRate(new TimerTask() {
      override def run() = {
        updateAuras()
      }
    }, 0, msAuraStickiness)

    //for now send some brutes
    //sendBrute(OgreID)

  }

  def pauseGame() = {
    timer.cancel()
  }

  def reset() = {
    Game.game = new Game(map, brutes)
  }
}
