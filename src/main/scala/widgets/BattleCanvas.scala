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
import models.{BruteID, TrapID, Game, Coordinate}

object BattleCanvas {
  var canvas : BattleCanvas = null
  var decoderOptions = new BitmapFactory.Options()
  decoderOptions.inSampleSize = 8
  var trapDecoderOptions = new BitmapFactory.Options()
  decoderOptions.inSampleSize = 2
  lazy val backgroundImage = BitmapFactory.decodeResource(canvas.getResources(), R.drawable.map);
  lazy val bruteImages: Map[BruteID, List[Bitmap]] = (for (x <- BruteID.Factory.ids) yield 
    (x, x.imageList.map(y => BitmapFactory.decodeResource(canvas.getResources(), y, decoderOptions)))).toMap
  lazy val trapImages: Map[TrapID, Bitmap] = (for (x <- TrapID.Factory.ids ++ TrapID.Factory.openIds) yield 
    (x, BitmapFactory.decodeResource(canvas.getResources(), x.image, trapDecoderOptions))).toMap
  lazy val projImage = BitmapFactory.decodeResource(canvas.getResources(),R.drawable.arrow)
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
    
    for (trap <- Game.game.trapList) {
      val image = trapImages(trap.id)
      drawPositioned(image, trap, false)
    }
    for (brute <- Game.game.bruteList.filter(_.isAlive)) {
      //to do climbing stairs
      val image = bruteImages(brute.id)(brute.currentFrame)

      drawPositioned(image, brute, brute.facingRight)
    }
    for (proj <- Game.game.projList.filter(_.isActive)) {
      drawPositioned(projImage, proj, false)
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
    battleHandler.sleep(50)
  }
}
