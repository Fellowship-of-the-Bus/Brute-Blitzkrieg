package com.github.fellowship_of_the_bus
package bruteb
package models

import android.content.SharedPreferences

import java.util.{Timer, TimerTask}

object Game {
  var game: Game = null
  var res: android.content.res.Resources = null
  var map: MapInfo = null
  var mapId: MapID = null
  object Options{
    var firstGame = false
  }
  val ticksPerSecond = 20
  val msPerTick = 1000/ticksPerSecond          //20 ticks/sec
  val msPerCleanup = msPerTick*40     //cleanup every 2 secs
  val msAuraStickiness = msPerTick*10  //update auras every 10 ticks
}

trait GameListener {
  def gameOver(numStars:Int): Unit
}

class Game(val map: MapInfo, var mapID: MapID, var brutes: Vector[BruteID] = Vector[BruteID](null, null, null, null)) {
  val levelName = mapID.id
  var bruteList: List[BaseBrute] = List[BaseBrute]()
  var trapList: List[BaseTrap] = List[BaseTrap]()
  var projList: List[BaseProjectile] = List[BaseProjectile]()

  var timer: Timer = null

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
      val projs = trap.attack()
      projList = projs:::projList
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
    }, 0, Game.msPerTick)
    timer.scheduleAtFixedRate(new TimerTask() {
      override def run() = {
        cleanup()
      }
    }, 0, Game.msPerCleanup)
    timer.scheduleAtFixedRate(new TimerTask() {
      override def run() = {
        updateAuras()
      }
    }, 0, Game.msAuraStickiness/2)
  }

  def pauseGame() = {
    if (timer != null)
      timer.cancel()
  }

  def reset() = {
    pauseGame
    map.clear()
    Game.game = new Game(map, mapID, brutes)
    Game.game.setListener(listener)
    listener = null
  }

  def checkGameOver(): Boolean = {
    if (score >= map.threeStar) return true
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
