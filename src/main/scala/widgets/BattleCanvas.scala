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


import models.MapInfo
import models.{BruteID, TrapID, Game, BaseBrute}

object BattleCanvas {
  var canvas : BattleCanvas = null
  var decoderOptions = new BitmapFactory.Options()
  decoderOptions.inSampleSize = 8
  lazy val backgroundImage = BitmapFactory.decodeResource(canvas.getResources(), R.drawable.map);
  lazy val bruteImages: Map[BruteID, Bitmap] = (for (x <- BruteID.Factory.ids) yield
    (x, BitmapFactory.decodeResource(canvas.getResources(), x.image, decoderOptions))).toMap
  lazy val trapImages: Map[TrapID, Bitmap] = (for (x <- TrapID.Factory.ids ++ TrapID.Factory.openIds) yield
    (x, BitmapFactory.decodeResource(canvas.getResources(), x.image, decoderOptions))).toMap
}

class BattleCanvas(val map: MapInfo)(implicit context: Context) extends SView {
  Game.game.battleCanvas = this
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

  override def onDraw(canvas: Canvas) = {
    super.onDraw(canvas)
    val canvasX = getWidth();
    val canvasY = getHeight();
    val cellX = (canvasX/8f).toInt
    val cellY = (canvasY/4f).toInt

    val paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.WHITE);
    //canvas.drawPaint(paint);
    // Use Color.parseColor to define HTML colors
    paint.setColor(Color.parseColor("#CD5C5C"));
    //canvas.drawCircle(x / 2, y / 2, radius, paint);

    canvas.drawBitmap(backgroundImage, null, new Rect(0, 0, canvasX , canvasY), null);
    canvas.save()
    for (trap <- Game.game.trapList) {
      val image = trapImages(trap.id)
      drawPositioned(image, trap)
    }
    for (brute <- Game.game.bruteList.filter(_.isAlive)) {
      //to do climbing stairs

      drawLifebar(brute)
      val image = bruteImages(brute.id)
      android.util.Log.e("bruteb", "Draw brute at " + brute.x.toString + " " + brute.y.toString)
      drawPositioned(image, brute)
    }
    for (proj <- Game.game.projList) {
      val image = BitmapFactory.decodeResource(getResources(), proj.image)
      drawPositioned(image, proj)
    }
    canvas.restore()

    paint.setColor(Color.WHITE);
    //canvas.drawRect(0,0, getWidth(), 2*getHeight()/3,paint);
    def normX(x: Float) = (x * cellX).toInt
    def normY(y: Float) = (y * cellY).toInt

    def drawPositioned(image: Bitmap, gameObject: TopLeftCoordinates) = {
      canvas.drawBitmap(image, null, new Rect(normX(gameObject.x), normY(gameObject.y),
                    normX(gameObject.x + gameObject.width), normY(gameObject.y + gameObject.height)), null)
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
      error(s"drawing lifebar at $pos")(new LoggerTag("bruteb"))
    }
    battleHandler.sleep(100)
  }
}
