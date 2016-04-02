package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._

// import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import android.widget.ImageView
import android.content.Intent
import android.graphics.Canvas

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class BattleActivity extends BaseActivity {
  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg battle activity started")
    super.onCreate(savedState)

    import Game.game

    setContentView(
      new SLinearLayout {
        (new BattleCanvas(game.map)).<<(0,MATCH_PARENT).Weight(3).>>.here

        new SVerticalLayout {
          for (i <- 0 until 4) {
            SImageButton(game.brutes(i).image, {
            }).<<(WRAP_CONTENT, 0).Weight(1).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).adjustViewBounds(true)
          }
        }.<<(0,WRAP_CONTENT).Weight(1).>>.gravity(Gravity.RIGHT).here
      }
    )
  }
}
