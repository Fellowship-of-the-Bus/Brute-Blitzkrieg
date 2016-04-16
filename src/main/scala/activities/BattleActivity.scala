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
            new SLinearLayout {
              SImageButton(game.brutes(i).image, {
              	game.sendBrute(game.brutes(i))
              new STextView {
                text = game.brutes(i).name
                textSize = 16 dip
              }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.CENTER).here
            }.<<(MATCH_PARENT,0).Weight(1).>>.here
          }
        }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.LEFT).here
      }
    )
  }
  override def onResume() {
    super.onResume()
    Game.game.startGame
  }

  override def onStop() {
    super.onStop()
    Game.game.pauseGame()
  }

}
