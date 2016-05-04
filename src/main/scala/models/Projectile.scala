package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory, TopLeftCoordinates}
import lib.util.{TimerListener, TickTimer}

import rapture.json._
import rapture.json.jsonBackends.jackson._

sealed trait ProjectileID {
  def imageList: List[Int]
  def image = imageList(0)
}
case object ArrowProj extends ProjectileID {
  override def imageList = List(R.drawable.arrow)
}
case object PoisonProj extends ProjectileID {
  override def imageList = List(R.drawable.poison_gas)
}

case class ProjAttr(
  speed: Float)

object ProjIds {
  val ids = Vector(ArrowProj, PoisonProj)
}

abstract class BaseProjectile(val id: ProjectileID, val coord: Coordinate, val damage: Float) extends TopLeftCoordinates {
  var active = true
  def speed() : Float
  def direction(): (Float, Float) // Direction that the projectile should move in
  def isActive = active
  def move() = {
    val (dx, dy) = direction()
    coord.x += dx * speed()
    coord.y += dy * speed()
  }
  def deactivate() = {
    active = false
  }
  override def x = coord.x
  override def y = coord.y
  override def width = 0.4f
  override def height = 0.4f

  def image = id.image
}

abstract class TimedProjectile(pid: ProjectileID, pcoord:Coordinate, val source: BaseTrap, val target: BaseBrute, val numFrames: Int) extends BaseProjectile(pid, pcoord, 0) with TimerListener {
  //a projectile drawable that does not do damage but only gives something to draw
  //damage is done instantly when the projectile is fired 
  //for instance a lightning bolt will be drawn for a couple frames but the damage is done when the tower fires
  add(new TickTimer(numFrames, () => deactivate()))

  override def move() = {
    tickOnce()
  }

  def tickOnce() = {
    if (ticking()) {
      tick(1)
    } else {
      cancelAll()
    }
  }

} 

class ArrowProjectile(pid: ProjectileID, pcoord: Coordinate, pdamage: Float, val source: BaseTrap, val target: BaseBrute) extends BaseProjectile(pid, pcoord, pdamage) {
  def direction(): (Float, Float) = {
    val (dx, dy): (Float, Float) = (target.x - x, target.y - y)
    val norm: Float = math.sqrt(dx*dx + dy*dy).toFloat
    if (norm == 0) {
      (0,0)
    } else {
      (dx/norm, dy/norm)
    }
  }
  def speed() = 0.2f
  override def move() = {
    if (!target.isAlive) {
      deactivate
    }
    val (dx, dy) = (target.coord.x-coord.x, target.coord.y - coord.y)
    val norm = math.sqrt(dx*dx + dy*dy)
    //check if we collide 
    if (norm >= speed) {
      super.move()
    } else {
      target.hit(source, damage)
      deactivate()
    }
  }
}

class PoisonProjectile(pid:ProjectileID, pcoord: Coordinate, psource: BaseTrap, ptarget:BaseBrute) extends TimedProjectile(pid, pcoord, psource, ptarget, 10) {
  override def width = 1f
  override def height = 3/4f
  override def direction = (0,0)
  override def speed = 0f
}