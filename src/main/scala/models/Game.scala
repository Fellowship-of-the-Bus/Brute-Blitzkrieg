package com.github.fellowship_of_the_bus
package bruteb
package models

import android.content.SharedPreferences

import java.util.{Timer, TimerTask}

object Game {
  var game: Game = null
}

trait GameListener {
  def gameOver(numStars:Int): Unit
}

class Game(val map: MapInfo, var levelName: String, var brutes: Vector[BruteID] = Vector[BruteID](null, null, null, null)) {
  val ticksPerSecond = 20

  val msPerTick = 1000/ticksPerSecond          //20 ticks/sec
  val msPerCleanup = msPerTick*40     //cleanup every 2 secs
  val msAuraStickiness = msPerTick*10  //update auras every 10 ticks
  var bruteList: List[BaseBrute] = List[BaseBrute]()
  var trapList: List[BaseTrap] = List[BaseTrap]()
  var projList: List[BaseProjectile] = List[BaseProjectile]()

  var timer: Timer = null//new Timer()

  var currentGold = map.startingGold
  var score = 0
  var listener: GameListener = null

  //make the towers that the map needs
  for (y <- map.height - 1 to 0 by -1) {
    for (x <- 0 until map.width ) {
      val tile = map.tiles(y)(x)
      addTrapFromID(tile.floorTrapID, Coordinate(x,y))
      addTrapFromID(tile.wallTrapID, Coordinate(x,y))
    }
  }

  private def adjustTrapCoord(id: TrapID, coord: Coordinate) = id match {
    case _: FloorTrapID => coord.copy(y = coord.y + 3/4f)
    case _ => coord
  }

  def addTrapFromID(id: TrapID, coord:Coordinate) = {
    if (id != NoTrapID) {
      val trap = Trap(id, adjustTrapCoord(id, coord))
      trapList = trap::trapList
    }
  }

  // id should only be FloorTrapID or WallTrapID
  def removeTrap(id: TrapID, coord: Coordinate) = {
    val copy = adjustTrapCoord(id, coord)
    trapList = trapList.filter(x => x.coord != copy)
  }

  //function for sending brutes
  def sendBrute(id: BruteID) = synchronized {
    //check that we have enough gold
    if (currentGold >= BruteAttributeMap(id).goldCost) {
      val brute = Brute(id, new Coordinate(map.startTileCoord.x, map.startTileCoord.y))
      bruteList = brute::bruteList
      currentGold = currentGold - BruteAttributeMap(id).goldCost
    }
  }

  def tick() = {
    //order of actions:
    //check game over
    //move brutes
    //check for brutes at exit
    //regenerate brutes
    //expire effects
    //move projectiles
    //fire towers
    if (checkGameOver) {
      pauseGame()
      gameOver()
    }
    for (brute <- bruteList.filter(_.isAlive)) {
      brute.move()
      //if brute reached end
      if (map.getTile(brute.coord) eq map.getTile(map.endTileCoord)) {
        //just kill for now do something more sophiscated later
        brute.hp = -1
        score += 1
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
    pauseGame
    Game.game = new Game(map, levelName, brutes)
  }

  def checkGameOver(): Boolean = {
    for (brute <- brutes) {
      if (BruteAttributeMap(brute).goldCost <= currentGold) {
        return false
      }
    }
    if (bruteList.filter(_.isAlive).length != 0) return false
    true
  }

  def setListener(l: GameListener) {
    listener = l
  }

  def gameOver() = {
    val curNumStars = if( score >= map.threeStar) 3 else if (score >= map.twoStar) 2 else if (score >= map.oneStar) 1 else 0
    listener.gameOver(curNumStars)
  }
}
