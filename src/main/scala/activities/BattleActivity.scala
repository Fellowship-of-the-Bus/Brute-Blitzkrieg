package com.github.fellowship_of_the_bus.bruteb

import models._

import org.scaloid.common._

// import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.graphics.Color
import android.widget.GridView
import android.widget.ImageView
import android.content.{Intent, Context}
import android.graphics.Canvas
import android.view.View

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps

class BattleActivity extends BaseActivity with GameListener {
  var stars: List[SImageView] = null 
  var txt: STextView = null
  var popUp: SRelativeLayout = null
  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg battle activity started")
    super.onCreate(savedState)

    import Game.game
    game.setListener(this)
    stars = List(new SImageView(R.drawable.grey_star),new SImageView(R.drawable.grey_star),new SImageView(R.drawable.grey_star))
    txt = new STextView{
      text = "hi"
      textSize = 32 dip
    }
    setContentView(
      new SRelativeLayout{
        new SLinearLayout {
          (new BattleCanvas(game.map)).<<(0,MATCH_PARENT).Weight(3).>>.here
            
          new SVerticalLayout {
            for (i <- 0 until 4) {
              new SLinearLayout {
                SImageButton(game.brutes(i).image, {
                	game.sendBrute(game.brutes(i))
                }).<<(0,MATCH_PARENT).Weight(1).marginBottom(if (i == 3) 0 dip else 5 dip).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).adjustViewBounds(true).backgroundColor(Color.GRAY)
                new STextView {
                  text = game.brutes(i).name
                  textSize = 16 dip
                }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.CENTER).here
              }.<<(MATCH_PARENT,0).Weight(1).>>.here
            }
          }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.LEFT).here
        }.<<.fill.>>.here
        popUp = new SRelativeLayout{
          new SRelativeLayout{
            txt.<<.wrap.alignParentTop.centerHorizontal.>>.here
            val starsView = new SLinearLayout {
              stars(0).<<(50,50).>>.here
              stars(1).<<(50,50).>>.here
              stars(2).<<(50,50).>>.here
            }.<<.wrap.below(txt).centerHorizontal.>>.gravity(Gravity.CENTER).here
            SButton(R.string.ReturnButton, {Game.game.reset()
                                            finish()}).<<.wrap.below(starsView).centerHorizontal.>>
          }.<<.wrap.centerInParent.>>.backgroundColor(Color.BLACK).gravity(Gravity.CENTER).here
        }.<<.fill.>>.visibility(View.GONE).here
      }
    )
  }
  override def onResume() {
    super.onResume()
    Game.game.startGame
  }

  override def onStop() {
    super.onStop()
    Game.game.reset()
  }

  override def gameOver(numStars: Int): Unit = {
    import Game.game
    val data = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)
    val editor = data.edit()
    val storedNumStars = data.getInt(game.levelName.id, 0)
    if (numStars > storedNumStars) {
      editor.putInt(game.levelName.id, numStars)
      editor.commit()
    }
    this.runOnUiThread(() => {
      if (numStars > 0) {
        txt.text = getResources().getString(R.string.Win)
      } else {
        txt.text = getResources().getString(R.string.Lose)
      }
      for (index <- 0 to numStars-1) {
        stars(index).imageDrawable(R.drawable.star)
      }
      popUp.visibility(View.VISIBLE)
    })
    ()
  }
}
