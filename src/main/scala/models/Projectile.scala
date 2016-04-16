package com.github.fellowship_of_the_bus
package bruteb
package models

import scala.collection.mutable.Set


import lib.game.{IDMap, IDFactory, TopLeftCoordinates}

import rapture.json._
import rapture.json.jsonBackends.jackson._

trait ProjectileID
case object ArrowProj extends ProjectileID

case class ProjAttr(
  speed: Float)

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

  def image: Int
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
  def image = R.drawable.arrow
}