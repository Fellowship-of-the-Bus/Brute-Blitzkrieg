package com.github.fellowship_of_the_bus
package bruteb

import models._

import org.scaloid.common._

// import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.graphics.{Color, Point}
import android.widget.GridView
import android.widget.ImageView
import android.content.{Intent, Context}
import android.graphics.Canvas
import android.view.View

import java.util.concurrent.atomic.AtomicInteger

import scala.language.postfixOps


import lib.util.rand

class BattleActivity extends BaseActivity with GameListener {
  var stars: List[SImageView] = null
  var txt: STextView = null
  var hint: STextView = null
  var popUp: SRelativeLayout = null

  var nextLevelButton: SButton = null
  var homeButton: SButton = null
  var retryButton: SButton = null
  var returnButton: SButton = null

  override def onCreate(savedState: Bundle) {
    android.util.Log.e("bruteb", "Brute Blitzkrieg battle activity started")
    super.onCreate(savedState)

    import Game.game
    game.setListener(this)
    stars = List(new SImageView(R.drawable.grey_star),new SImageView(R.drawable.grey_star),new SImageView(R.drawable.grey_star))
    txt = new STextView{
      text = "hi"
      textSize = 32 sp
    }
    setContentView(
      new SRelativeLayout{
        new SLinearLayout {
          (new BattleCanvas(game.map)).<<(0,MATCH_PARENT).Weight(3).>>.here
          new SVerticalLayout {
            // make a button and text label for each selected brute
            // filter to put all selected brutes together at the top
            val brutes = game.brutes.filter(_ != null)
            for (i <- 0 until brutes.length) {
              new SLinearLayout {
                SImageButton(brutes(i).image, {
                  game.sendBrute(brutes(i))
                }).<<(0,MATCH_PARENT).Weight(1).marginBottom(if (i == game.brutes.length-1) 0 dip else 5 dip).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).adjustViewBounds(true).backgroundColor(Color.GRAY)
                new SVerticalLayout {
                  new STextView {
                    val name: CharSequence = brutes(i).name
                    val cost = BruteAttributeMap(brutes(i)).goldCost
                    text = s"${name}"
                    textSize = 16 sp
                  }.<<.wrap.Gravity(Gravity.CENTER).>>.gravity(Gravity.CENTER).here
                  new SLinearLayout {
                    SImageView(R.drawable.gold).<<(40,40)
                    new STextView {
                      val cost = BruteAttributeMap(brutes(i)).goldCost
                      text = cost.toString
                      textSize = 16 sp
                    }.<<.wrap.Gravity(Gravity.CENTER).>>.here
                  }.<<.fw.Gravity(Gravity.CENTER).>>.gravity(Gravity.CENTER).here
                }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.CENTER).here
              }.<<(MATCH_PARENT,0).Weight(1).>>.gravity(Gravity.CENTER).here
            }
            // make a button for each unselected brute and make it invisible to keep the spacing consistent
            for (i <- 0 until game.brutes.length - brutes.length) {
              new SVerticalLayout {
                SImageButton(brutes(0).image).<<(0,MATCH_PARENT).Weight(1).marginBottom(if (i+brutes.length == game.brutes.length) 0 dip else 5 dip).>>.scaleType(ImageView.ScaleType.CENTER_INSIDE).adjustViewBounds(true).visibility(View.INVISIBLE)
              }.<<(MATCH_PARENT,0).Weight(1).>>.gravity(Gravity.CENTER).here
            }
          }.<<(0,MATCH_PARENT).Weight(1).>>.gravity(Gravity.LEFT).here
        }.<<.fill.>>.here
        popUp = new SRelativeLayout{
          new SView().<<.fill.>>.alpha(0.5f).backgroundColor(Color.BLACK).here
          new SRelativeLayout{
            txt.<<.wrap.alignParentTop.centerHorizontal.>>.here
            val starsView = new SLinearLayout {
              stars(0).<<(50,50).>>.here
              stars(1).<<(50,50).>>.here
              stars(2).<<(50,50).>>.here
            }.<<.wrap.below(txt).centerHorizontal.>>.gravity(Gravity.CENTER).here
            val point = new Point()
            getWindowManager().getDefaultDisplay().getSize(point)
            hint = new STextView{
              text = "hi"
              textSize = 32
            }.<<(point.x/2,WRAP_CONTENT).below(starsView).centerHorizontal.>>.here
            val buttons = new SLinearLayout {
              import MapID.Factory.ids
              nextLevelButton = SButton(R.string.NextLevelButton, {
                val curIndex = ids.indexOf(Game.game.mapID)
                if (curIndex != -1 && curIndex < MapID.Factory.ids.length-1) {
                  finish()
                  val mapID = ids(curIndex + 1)
                  switchScreen(mapID, maps(mapID))
                }
              }).<<.wrap.>>
              retryButton = SButton(R.string.RetryButton, {
                Game.game.reset()
                Game.game.startGame
                popUp.visibility(View.GONE)
              }).<<.wrap.>>
              returnButton = SButton(R.string.ReturnButton, {
                                            finish()}).<<.wrap.>>
              homeButton = SButton(R.string.HomeButton,{
                finish()
                switchScreen(classOf[MainActivity], true, true)
              }).<<.wrap.>>
            }.<<.wrap.below(hint).centerInParent.>>.gravity(Gravity.CENTER_HORIZONTAL).here
          }.<<.wrap.centerInParent.>>.backgroundColor(Color.BLACK).gravity(Gravity.CENTER_HORIZONTAL).here
        }.<<.fill.>>.visibility(View.GONE).here
      }
    )
  }
  override def onResume() = {
    Game.game.startGame
    super.onResume()
  }

  override def onBackPressed() = {
    super.onBackPressed()
    Game.game.reset()
  }

  override def finish() = {
    Game.game.reset()
    super.finish()
  }
  override def gameOver(numStars: Int): Unit = {
    import Game.game
    val data = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)
    val options = getSharedPreferences("Options", Context.MODE_PRIVATE)
    val editor = data.edit()
    val storedNumStars = data.getInt(game.levelName, 0)
    if (numStars > storedNumStars) {
      editor.putInt(game.levelName, numStars)
      editor.commit()
    }

    val optionsEditor = options.edit()
    if (checkTutorialOver()) {
      optionsEditor.putBoolean("ViewTutorial", false)
      optionsEditor.commit()
      Game.Options.tutorial = false
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
      import MapID.Factory.ids
      val curIndex = ids.indexOf(Game.game.mapID)
      if (curIndex == -1 || curIndex >= MapID.Factory.ids.length-1 || numStars <= 0) {
        nextLevelButton.visibility(View.GONE)
      } else {
        nextLevelButton.visibility(View.VISIBLE)
      }

      // 3 star win, give no hint
      if (numStars == 3 || !Game.Options.tutorial) {
        hint.visibility(View.GONE)
      } else {
        updateHint()
        hint.visibility(View.VISIBLE)
      }
      popUp.visibility(View.VISIBLE)
    })
    ()
  }

  def checkTutorialOver(): Boolean = {
    val options = getSharedPreferences("Options", Context.MODE_PRIVATE)

    if (!options.getBoolean("ViewTutorial", true)) {
      return true
    }
    val data = getSharedPreferences("UserProgress", Context.MODE_PRIVATE)

    for (level <- 0 until Game.tutorialLevels ) {
      if (data.getInt(MapID.Factory.ids(level).id, 0) == 0) {
        return false
      }
    }
    return true
  }

  def updateHint() = {
    import MapID.Factory.ids
    val curIndex = ids.indexOf(Game.game.mapID)
    if (curIndex == -1 || curIndex >= Game.tutorialLevels) {
      hint.text = rand(MapID.randomPostHintList)
    } else {
      hint.text = Game.game.mapID.postHint
    }
  }
}
