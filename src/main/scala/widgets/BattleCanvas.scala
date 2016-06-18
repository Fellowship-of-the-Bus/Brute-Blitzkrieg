package com.github.fellowship_of_the_bus
package bruteb

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.os.Bundle;
import android.view.View;
import android.graphics.Rect;

import org.scaloid.common._

//import lib.game.TopLeftCoordinates
import lib.game.{IDMap, IDFactory, TopLeftCoordinates}

import android.os.{Handler, Message}

import scala.math.{atan, toDegrees, atan2}

import models.{BruteID, TrapID, ProjectileID, ProjIds, Game, BaseBrute, Coordinate, BaseProjectile, MapInfo, MapID}

import scala.language.postfixOps

object BattleCanvas {
  var canvas : BattleCanvas = null
  var decoderOptions = new BitmapFactory.Options()
  decoderOptions.inSampleSize = 2
  var trapDecoderOptions = new BitmapFactory.Options()
  trapDecoderOptions.inSampleSize = 2
  lazy val backgroundImage = BitmapFactory.decodeResource(canvas.getResources(), R.drawable.map);
  lazy val bruteImages: Map[BruteID, List[Bitmap]] = (for (x <- BruteID.Factory.ids) yield
    (x, x.imageList.map(y => BitmapFactory.decodeResource(canvas.getResources(), y, decoderOptions)))).toMap
  lazy val trapImages: Map[TrapID, Bitmap] = (for (x <- TrapID.Factory.ids ++ TrapID.Factory.openIds) yield
    (x, BitmapFactory.decodeResource(canvas.getResources(), x.image, trapDecoderOptions))).toMap
  lazy val projImages: Map[ProjectileID, List[Bitmap]] =  (for (x <- ProjIds.ids) yield
    (x, x.imageList.map(y => BitmapFactory.decodeResource(canvas.getResources(), y, decoderOptions)))).toMap
  lazy val exitImage =  BitmapFactory.decodeResource(canvas.getResources(), R.drawable.door, decoderOptions)
  lazy val entranceImage =  BitmapFactory.decodeResource(canvas.getResources(), R.drawable.door2, decoderOptions)
  lazy val goldImage = BitmapFactory.decodeResource(canvas.getResources(), R.drawable.gold, decoderOptions)
}

class BattleCanvas(val map: MapInfo, drawGrid: Boolean = false)(implicit context: Context) extends SView {
  import BattleCanvas._
  canvas = this
  val battleHandler = new BattleHandler()
  class BattleHandler extends Handler {
    override def handleMessage(m: Message) = {
      BattleCanvas.this.invalidate()
    }
    def sleep(delay: Long): Unit = {
      this.removeMessages(0)
      val _ = sendMessageDelayed(obtainMessage(0), delay)
    }
  }

  lazy val canvasX = getWidth()
  lazy val canvasY = getHeight()
  lazy val cellX = (canvasX/MapID.width).toInt
  lazy val cellY = (canvasY/MapID.height).toInt
  override def onDraw(canvas: Canvas) = {
    super.onDraw(canvas)

    val paint = new Paint()
    paint.setStyle(Paint.Style.FILL)
    paint.setColor(Color.WHITE)
    //canvas.drawPaint(paint);
    // Use Color.parseColor to define HTML colors
    paint.setColor(Color.parseColor("#000000"))
    paint.setTextAlign(Paint.Align.LEFT)
    paint.setTextSize(40)
    //canvas.drawCircle(x / 2, y / 2, radius, paint);

    canvas.drawBitmap(backgroundImage, null, new Rect(0, 0, canvasX , canvasY), null)
    canvas.drawBitmap(goldImage, null, new Rect(0, 0, 50, 50), null)
    canvas.drawText(Game.game.currentGold.toString, 60, 40, paint)
    canvas.drawBitmap(exitImage, null,
        new Rect(normX(Game.game.map.endTileCoord.x),
            normY(Game.game.map.endTileCoord.y),
            normX(Game.game.map.endTileCoord.x + 1),
            normY(Game.game.map.endTileCoord.y + 0.75f)), null)
    canvas.drawBitmap(entranceImage, null,
        new Rect(normX(Game.game.map.startTileCoord.x),
            normY(Game.game.map.startTileCoord.y),
            normX(Game.game.map.startTileCoord.x + 1),
            normY(Game.game.map.startTileCoord.y + 0.75f)), null)


    for (trap <- Game.game.trapList.reverse) {
      val image = trapImages(trap.id)
      drawPositioned(image, trap, false)
    }
    for (proj <- Game.game.projList.filter(_.isActive)) {
      val image = projImages(proj.id)(0)
      //drawPositioned(image, proj, false)
      drawRotated(image, proj)
    }
    for (brute <- Game.game.bruteList.filter(_.isAlive)) {
      //to do climbing stairs
      drawLifebar(brute)
      val image = bruteImages(brute.id)(brute.currentFrame)

      drawPositioned(image, brute, brute.facingRight)
    }
    // draw grid for level editor
    if (drawGrid) {
      paint.setColor(Color.RED)
      paint.setStrokeWidth(2 dip)
      for (row <- 0 to MapID.height) {
        canvas.drawLine(0, row*cellY, canvasX, row*cellY, paint)
      }
      for (col <- 0 to MapID.width) {
        canvas.drawLine(col*cellX, 0, col*cellX, canvasY, paint)
      }
    }

    paint.setColor(Color.WHITE);
    //canvas.drawRect(0,0, getWidth(), 2*getHeight()/3,paint);
    def normX(x: Float) = (x * cellX).toInt
    def normY(y: Float) = (y * cellY).toInt

    def drawPositioned(image: Bitmap, gameObject: TopLeftCoordinates, flip: Boolean) = {
      var drawX1 = gameObject.x
      var drawX2 = drawX1 + gameObject.width
      canvas.save()
      if (flip) {
        canvas.scale(-1f,1f)
        drawX1 = drawX2 * -1
        drawX2 = gameObject.x * -1
      }
      canvas.drawBitmap(image, null, new Rect(normX(drawX1), normY(gameObject.y),
                    normX(drawX2), normY(gameObject.y + gameObject.height)), null)
      canvas.restore()
    }
    def drawRotated(image: Bitmap, projectile: BaseProjectile) : Unit= {
      val (dx, dy) = projectile.direction
      //if no direction specified, just draw it without rotations
      if (dx == 0 && dy == 0) {
        drawPositioned(image, projectile, false)
        return
      }
      canvas.save()
      val angle : Float = {
        if (dx == 0 && dy < 0) {
          90f
        } else if (dx == 0 && dy > 0) {
          270f
        } else {
          (toDegrees(atan2(-dy, dx)).toFloat + 360) % 360
        }
      }
      var (cx, cy) = projectile.centerCoord()
      cx = normX(cx)
      cy = normY(cy)
      canvas.rotate(-angle, cx, cy)
      drawPositioned(image, projectile, false)
      canvas.restore()
    }

    def drawLifebar(brute: BaseBrute) = {
      // vertical distance between a lifebar and the top left corner of the image
      val dist = 10
      val hp = brute.hp
      val maxHp = brute.maxHP
      val (left, top, right, _) = brute.coordinates
      val width = right-left
      val height = 5

      val x1 = normX(left)
      val x2 = normX(right)
      val y1 = normY(top)-dist
      val y2 = y1-height

      val pos = new Rect(x1, y1, x2, y2)
      val paint = new Paint();
      paint.setStyle(Paint.Style.STROKE)
      paint.setColor(Color.BLACK)
      canvas.drawRect(pos, paint)

      pos.set(x1, y1, normX(left+width*hp/maxHp), y2)
      paint.setStyle(Paint.Style.FILL)
      paint.setColor(Color.RED)
      canvas.drawRect(pos, paint)
    }
    battleHandler.sleep(50)
  }
}
