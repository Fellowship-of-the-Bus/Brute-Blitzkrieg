package com.github.fellowship_of_the_bus.bruteb

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import org.scaloid.common._

class BattleCanvas(implicit context: Context) extends SView {
  override def onDraw(canvas: Canvas) = {
    super.onDraw(canvas)
    val x = getWidth();
    val y = getHeight();
    val radius = 100;
    val paint = new Paint();
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(Color.WHITE);
    canvas.drawPaint(paint);
    // Use Color.parseColor to define HTML colors
    paint.setColor(Color.parseColor("#CD5C5C"));
    canvas.drawCircle(x / 2, y / 2, radius, paint);
  }
}
