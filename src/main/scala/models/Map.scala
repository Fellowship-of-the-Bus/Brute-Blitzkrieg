package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set



case class Tile(val floorTrapID: Int, val wallTrapID: Int) {
  val bruteList: Set[Brute] = Set[Brute]()

  def register(e: Brute) = {
        bruteList += e
    }

  def deregister(e: Brute) = {
      bruteList -= e
  }
}

case class MapInfo(
  tiles: List[List[Tile]],
  startTileCoord: (Int, Int),
  endTileCoord: (Int, Int))

// tiles have coordinates with bottom left = (0,0)
class LevelMap() {
  def apply(num: Int) = levelMap(num)
  val levelMap = collection.immutable.HashMap(1 -> 
  MapInfo(List(List(Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0)),
               List(Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0), Tile(0,0))
               ),
          (0,4), (1,4)
          )
  )
}

class GameMap(num : Int) {
  val brute = new Brute()
  val m = new LevelMap()
  val tiles = m(num).tiles
  val startTileCoord = m(num).startTileCoord
  val endTileCoord = m(num).endTileCoord
}