package com.github.fellowship_of_the_bus

// import android.app.Activity
import org.scaloid.common._
import android.graphics.{Color,ColorMatrix,ColorMatrixColorFilter}
import android.widget.ImageView

package object bruteb {
  def makeClickable(view: ImageView, clickable: Boolean): view.type = {
    view.clickable = clickable
    val colors = new ColorMatrix()
    colors.setSaturation(if (view.clickable) 1 else 0)
    val filter = new ColorMatrixColorFilter(colors)
    view.setColorFilter(filter)
    view
  }
}
