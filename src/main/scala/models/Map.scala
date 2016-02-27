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

class Map(num : Int) {
	val brute = new Brute()

}